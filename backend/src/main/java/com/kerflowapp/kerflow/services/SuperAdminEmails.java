package com.kerflowapp.kerflow.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Liste des emails d'utilisateurs ayant le role SUPER_ADMIN, alimentee par la configuration
 * ({@code kerflow.super-admin-emails}, cf. la variable d'environnement SUPER_ADMIN_EMAILS).
 * Par defaut la liste est vide : aucun compte n'est super-admin tant qu'elle n'est pas renseignee.
 * P1 : a remplacer par un vrai role en base + UserRole enum + verification via authStore.
 */
@Component
public class SuperAdminEmails {

    private final List<String> emails;

    public SuperAdminEmails(@Value("${kerflow.super-admin-emails:}") List<String> emails) {
        this.emails = emails.stream()
            .map(email -> email.trim().toLowerCase(Locale.ROOT))
            .filter(email -> !email.isEmpty())
            .toList();
    }

    public boolean contains(String login) {
        return login != null && emails.contains(login.trim().toLowerCase(Locale.ROOT));
    }
}
