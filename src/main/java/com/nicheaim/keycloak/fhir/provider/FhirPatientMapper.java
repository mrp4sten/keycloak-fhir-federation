package com.nicheaim.keycloak.fhir.provider;

import static com.nicheaim.keycloak.fhir.model.FhirPatientRep.BIRTH_DATE;
import static com.nicheaim.keycloak.fhir.model.FhirPatientRep.identifierKey;
import static com.nicheaim.keycloak.fhir.model.FhirPatientRep.telecomKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

import com.nicheaim.keycloak.fhir.model.FhirPatientRep;

/**
 * Maps HAPI FHIR R4 {@link Patient} resources into the internal
 * {@link FhirPatientRep} DTO.
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Extracts stable identifiers (resource id, identifiers, telecoms,
 * birthDate).</li>
 * <li>Applies light normalization (trim) and ignores blank values.</li>
 * <li>Does NOT call external services nor enforce username strategies (left for
 * the service layer).</li>
 * </ul>
 *
 * <h2>Attribute conventions</h2>
 * <ul>
 * <li>{@code birthDate} stored under {@link FhirPatientRep#BIRTH_DATE}.</li>
 * <li>{@code identifier:{system}} via
 * {@link FhirPatientRep#identifierKey(String)}.</li>
 * <li>{@code telecom:{system}} via
 * {@link FhirPatientRep#telecomKey(String)}.</li>
 * </ul>
 *
 * <h3>Username</h3>
 * The resulting DTO sets {@code username} to the resource id by default. A
 * service layer may replace it
 * according to a configured selector (e.g., a specific Identifier system).
 */
public final class FhirPatientMapper {

  private FhirPatientMapper() {
    // utility class
  }

  /**
   * Builds a {@link FhirPatientRep} from a {@link Patient}.
   *
   * @param patient non-null Patient resource
   * @return mapped DTO or {@code null} if input is null
   */
  public static FhirPatientRep fromPatient(Patient patient) {
    if (patient == null)
      return null;

    final String id = extractId(patient);

    // Names & email
    final NameParts names = extractNames(patient);
    final String email = extractEmail(patient);

    // Attributes
    final Map<String, String> attrs = new HashMap<>();
    collectBirthDate(patient, attrs);
    collectIdentifiers(patient, attrs);
    collectTelecoms(patient, attrs);

    // username defaults to id; can be overridden upstream
    return new FhirPatientRep(id, id, names.firstName, names.lastName, email, true, attrs);
  }

  /**
   * Returns the first Patient from a Bundle, if present.
   */
  public static FhirPatientRep firstFromBundle(Bundle bundle) {
    if (bundle == null)
      return null;
    for (Bundle.BundleEntryComponent e : bundle.getEntry()) {
      if (e.getResource() instanceof Patient p) {
        return fromPatient(p);
      }
    }
    return null;
  }

  // ---------- helpers ----------

  private static String extractId(Patient p) {
    if (p.getIdElement() != null && p.getIdElement().hasIdPart()) {
      return p.getIdElement().getIdPart();
    }
    // Fallback: rarely needed, but keeps us safe if idPart is missing
    return p.hasId() ? p.getId() : null;
  }

  private static NameParts extractNames(Patient p) {
    if (p.getName().isEmpty())
      return NameParts.EMPTY;

    final HumanName hn = p.getNameFirstRep();
    String given = null;
    // Prefer the first non-blank given
    List<?> givens = hn.getGiven();
    if (givens != null && !givens.isEmpty()) {
      var v = hn.getGiven().get(0).getValue();
      if (v != null && !v.isBlank())
        given = v.trim();
    }
    String family = hn.getFamily();
    if (family != null)
      family = family.trim();

    return new NameParts(given, family);
  }

  private static String extractEmail(Patient p) {
    for (ContactPoint cp : p.getTelecom()) {
      if (cp.getSystem() == ContactPoint.ContactPointSystem.EMAIL) {
        String value = cp.getValue();
        if (value != null && !value.isBlank()) {
          return value.trim();
        }
      }
    }
    return null;
  }

  private static void collectBirthDate(Patient p, Map<String, String> out) {
    if (p.hasBirthDateElement() && p.getBirthDateElement().hasValue()) {
      String s = p.getBirthDateElement().asStringValue();
      if (s != null && !s.isBlank()) {
        out.put(BIRTH_DATE, s);
      }
    }
  }

  private static void collectIdentifiers(Patient p, Map<String, String> out) {
    for (Identifier idf : p.getIdentifier()) {
      String sys = idf.getSystem();
      String val = idf.getValue();
      if (nonBlank(sys) && nonBlank(val)) {
        out.put(identifierKey(sys), val.trim());
      }
    }
  }

  private static void collectTelecoms(Patient p, Map<String, String> out) {
    for (ContactPoint cp : p.getTelecom()) {
      if (cp.getSystem() != null && cp.hasValue()) {
        String sys = cp.getSystem().toCode(); // e.g. "email", "phone"
        String val = cp.getValue();
        if (nonBlank(sys) && nonBlank(val)) {
          out.put(telecomKey(sys.trim()), val.trim());
        }
      }
    }
  }

  private static boolean nonBlank(String s) {
    return s != null && !s.isBlank();
  }

  /** small value object for clarity */
  private record NameParts(String firstName, String lastName) {
    static final NameParts EMPTY = new NameParts(null, null);

    private NameParts {
      // normalize
      if (firstName != null)
        firstName = firstName.trim();
      if (lastName != null)
        lastName = lastName.trim();
    }
  }
}
