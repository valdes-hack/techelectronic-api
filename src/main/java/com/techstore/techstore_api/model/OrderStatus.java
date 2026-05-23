package com.techstore.techstore_api.model;

public enum OrderStatus {
    EN_ATTENTE,         // Commande créée, paiement non reçu
    PAIEMENT_CONFIRME,  // Argent reçu (Mobile Money validé)
    EN_PREPARATION,     // Dans l'entrepôt
    EXPEDIE,            // Chez le livreur
    LIVRE,              // Reçu par le client
    ANNULE              // Commande annulée
}