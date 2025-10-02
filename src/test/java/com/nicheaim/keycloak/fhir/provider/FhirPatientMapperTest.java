package com.nicheaim.keycloak.fhir.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nicheaim.keycloak.fhir.model.FhirPatientRep;

/**
 * Unit tests for {@link FhirPatientMapper}.
 *
 * Scope:
 * - Ensures robust mapping from HAPI FHIR R4 Patient to FhirPatientRep DTO.
 * - Covers trimming, attribute conventions, fallbacks, and null-safety.
 */
class FhirPatientMapperTest {

  @Test
  @DisplayName("Maps a typical Patient to DTO")
  void mapsPatientResourceToRep() {
    Patient p = new Patient();
    p.setId("p1");
    p.addName(new HumanName().setFamily("Ramírez").setGiven(List.of(new StringType("Mauricio"))));
    p.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue("mau@example.com"));
    p.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue("+52551111"));
    p.addIdentifier(new Identifier().setSystem("urn:sys:curp").setValue("CURP123"));
    p.setBirthDateElement(new DateType("1990-01-01"));

    FhirPatientRep rep = FhirPatientMapper.fromPatient(p);

    assertNotNull(rep);
    assertEquals("p1", rep.id());
    assertEquals("Mauricio", rep.firstName());
    assertEquals("Ramírez", rep.lastName());
    assertEquals("mau@example.com", rep.email());
    assertEquals("CURP123", rep.attributes().get("identifier:urn:sys:curp"));
    assertEquals("+52551111", rep.attributes().get("telecom:phone"));
    assertEquals("1990-01-01", rep.attributes().get("birthDate"));
  }

  @Test
  @DisplayName("Trims names and picks first given name")
  void trimsNamesAndPicksFirstGiven() {
    Patient p = new Patient();
    p.setId("px");
    p.addName(new HumanName()
        .setFamily("  Doe  ")
        .setGiven(List.of(new StringType("  Jane  "), new StringType("Extra"))));

    FhirPatientRep rep = FhirPatientMapper.fromPatient(p);

    assertEquals("Jane", rep.firstName());
    assertEquals("Doe", rep.lastName());
  }

  @Test
  @DisplayName("Email: picks first non-blank and ignores blanks")
  void emailSelectionIgnoresBlank() {
    Patient p = new Patient();
    p.setId("px");
    p.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue("   "));
    p.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue("user@example.org"));

    FhirPatientRep rep = FhirPatientMapper.fromPatient(p);

    assertEquals("user@example.org", rep.email());
  }

  @Test
  @DisplayName("Telecoms and identifiers are flattened with proper keys")
  void telecomsAndIdentifiersFlattening() {
    Patient p = new Patient();
    p.setId("px");
    p.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue("  +123  "));
    p.addIdentifier(new Identifier().setSystem("urn:sys:foo").setValue("VAL123"));

    FhirPatientRep rep = FhirPatientMapper.fromPatient(p);

    assertEquals("+123", rep.attributes().get("telecom:phone"));
    assertEquals("VAL123", rep.attributes().get("identifier:urn:sys:foo"));
  }

  @Test
  @DisplayName("BirthDate is optional; missing birthDate is not added")
  void birthDateOptional() {
    Patient p = new Patient();
    p.setId("px");

    FhirPatientRep rep = FhirPatientMapper.fromPatient(p);

    assertFalse(rep.attributes().containsKey("birthDate"));
  }

  @Test
  @DisplayName("Username defaults to resource id")
  void usernameDefaultsToId() {
    Patient p = new Patient();
    p.setId("resource-123");

    FhirPatientRep rep = FhirPatientMapper.fromPatient(p);

    assertEquals("resource-123", rep.username());
  }

  @Test
  @DisplayName("Attributes map is immutable (defensive copy in DTO)")
  void attributesMapIsImmutable() {
    Patient p = new Patient();
    p.setId("p1");
    p.addIdentifier(new Identifier().setSystem("urn:sys:a").setValue("A"));

    FhirPatientRep rep = FhirPatientMapper.fromPatient(p);

    UnsupportedOperationException ex = assertThrowsExactly(UnsupportedOperationException.class,
        () -> rep.attributes().put("x", "y"));
    assertNotNull(ex);
  }

  @Test
  @DisplayName("firstFromBundle returns first Patient or null when absent")
  void firstFromBundleBehavior() {
    // Case 1: with patient
    Patient px = new Patient();
    px.setId("pX");
    px.addName(new HumanName().setFamily("B.").addGiven("Kira"));
    px.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue("kira@example.com"));

    Bundle withPatient = new Bundle();
    withPatient.addEntry().setResource(px);

    FhirPatientRep rep1 = FhirPatientMapper.firstFromBundle(withPatient);
    assertNotNull(rep1);
    assertEquals("pX", rep1.id());
    assertEquals("Kira", rep1.firstName());
    assertEquals("kira@example.com", rep1.email());

    // Case 2: empty bundle
    Bundle empty = new Bundle();
    assertNull(FhirPatientMapper.firstFromBundle(empty));
  }

  @Test
  @DisplayName("Null patient returns null")
  void nullPatientReturnsNull() {
    assertNull(FhirPatientMapper.fromPatient(null));
  }

  @Test
  @DisplayName("Unicode and accents are preserved")
  void unicodeIsPreserved() {
    Patient p = new Patient();
    p.setId("ñ-Íd");
    p.addName(new HumanName().setFamily("García").addGiven("María-José"));
    p.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue("m.josé@ejemplo.mx"));

    FhirPatientRep rep = FhirPatientMapper.fromPatient(p);

    assertEquals("ñ-Íd", rep.id());
    assertEquals("María-José", rep.firstName());
    assertEquals("García", rep.lastName());
    assertEquals("m.josé@ejemplo.mx", rep.email());
  }
}
