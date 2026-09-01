package com.kerflowapp.kerflow.services.sirene;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * A company as declared in the French company registry, parsed from the
 * recherche-entreprises API. Raw facts only: interpretation (size bucket, business
 * category) belongs to the enrichers.
 */
@Builder
public record CompanyIdentity(
    String siren,
    String nomComplet,
    String nomRaisonSociale,
    String sigle,
    List<String> listeEnseignes,
    String nomCommercial,
    String codePostalSiege,
    /*
     * INSEE workforce bracket code ("00", "01"... "53"), or "NN" when the company
     * declares no workforce — frequent for small businesses.
     */
    String trancheEffectifSalarie,
    Integer anneeTrancheEffectifSalarie,
    LocalDate dateCreation,
    Integer nombreEtablissementsOuverts,
    /*
     * NAF/APE code of the main activity, e.g. "86.90A". The API returns the code
     * only, never a label. Beware: legacy rev.1 codes ("85.1J") still show up.
     */
    String activitePrincipale,
    String sectionActivitePrincipale,
    String etatAdministratif,
    boolean estEntrepreneurIndividuel,
    boolean estAssociation

) {
}
