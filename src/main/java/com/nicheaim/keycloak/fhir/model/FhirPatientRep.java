package com.nicheaim.keycloak.fhir.model;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable representation of a FHIR Patient resource adapted to a Keycloak
 * user.
 * <p>
 * This class is used as a neutral DTO (Data Transfer Object) between the FHIR
 * datastore
 * and the Keycloak User Storage SPI. It does not depend on Keycloak classes,
 * making it
 * easy to test and mock.
 * </p>
 *
 * <h2>Conventions for attributes:</h2>
 * <ul>
 * <li><b>birthDate</b>: the patient birth date in yyyy-MM-dd format.</li>
 * <li><b>identifier:{system}</b>: identifier values keyed by system, e.g.
 * {@code identifier:urn:sys:curp}.</li>
 * <li><b>telecom:{system}</b>: telecom values keyed by system, e.g.
 * {@code telecom:email}, {@code telecom:phone}.</li>
 * </ul>
 *
 * <h2>Typical usage:</h2>
 * 
 * <pre>{@code
 * FhirPatientRep rep = new FhirPatientRep(
 *     "p1", "p1", "Mauricio", "Ramírez", "mau@example.com", true,
 *     Map.of(
 *         FhirPatientRep.identifierKey("urn:sys:curp"), "CURP123",
 *         FhirPatientRep.telecomKey("phone"), "+52551111",
 *         FhirPatientRep.BIRTH_DATE, "1990-01-01"));
 * }</pre>
 *
 * @param id            The resource ID of the FHIR Patient (mandatory).
 * @param username      The Keycloak username for the patient. Defaults to
 *                      {@code id} if blank.
 * @param firstName     Patient's given name (first name).
 * @param lastName      Patient's family name (last name).
 * @param email         Patient's email address from FHIR telecom.
 * @param emailVerified Whether the email is considered verified.
 * @param attributes    A flattened map of additional attributes (identifiers,
 *                      telecoms, birth date).
 */
public record FhirPatientRep(
    String id,
    String username,
    String firstName,
    String lastName,
    String email,
    boolean emailVerified,
    Map<String, String> attributes) {
  /** Standard attribute key for birthDate. */
  public static final String BIRTH_DATE = "birthDate";

  /**
   * Compact constructor to enforce invariants:
   * - id cannot be null.
   * - username defaults to id if null or blank.
   * - attributes map is defensively copied and never null.
   */
  public FhirPatientRep {
    Objects.requireNonNull(id, "id must not be null");

    if (username == null || username.isBlank()) {
      username = id;
    }

    attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
  }

  /**
   * @return The concatenated full name (firstName + lastName), trimmed.
   */
  public String fullName() {
    String fn = firstName == null ? "" : firstName.trim();
    String ln = lastName == null ? "" : lastName.trim();
    return (fn + " " + ln).trim();
  }

  /**
   * Retrieves an attribute by key.
   *
   * @param key attribute key
   * @return value or null if not found
   */
  public String attribute(String key) {
    return attributes.get(key);
  }

  /**
   * Checks if an attribute exists and has a non-blank value.
   *
   * @param key attribute key
   * @return true if the attribute exists and is non-blank
   */
  public boolean hasAttribute(String key) {
    String v = attributes.get(key);
    return v != null && !v.isBlank();
  }

  /**
   * Builds a standard attribute key for an Identifier system.
   *
   * @param system FHIR Identifier.system value
   * @return key in the form {@code identifier:{system}}
   */
  public static String identifierKey(String system) {
    return "identifier:" + system;
  }

  /**
   * Builds a standard attribute key for a ContactPoint (telecom) system.
   *
   * @param system FHIR ContactPoint.system value
   * @return key in the form {@code telecom:{system}}
   */
  public static String telecomKey(String system) {
    return "telecom:" + system;
  }
}
