## TD — Principe Ouvert/Fermé (OCP)

`CallHistory` et `InternetSessionHistory` dupliquaient le stockage par identifiant, l'ajout d'une session et le calcul d'un total. `UsageHistory` est leur contrat commun : il s'agit d'une seule interface, implémentée par ces deux historiques. `Subscriber` est une classe abstraite qui centralise l'identité, les coordonnées et le tarif communs ; `PhoneSubscriber` et `ISPSubscriber` appliquent leurs règles de facturation propres. Les abonnés dépendent du contrat `UsageHistory`, pas des classes concrètes.

Pour ajouter un nouvel historique, créez une classe qui implémente `UsageHistory` et fournissez-la à un abonné ; il n'est pas nécessaire de modifier les historiques existants. Pour ajouter une nouvelle catégorie d'abonné, créez une sous-classe de `Subscriber` et implémentez `calculateBill()`.

Exemple : `PhoneSubscriber phone = new PhoneSubscriber(new CallHistory()); phone.setSubscriberId(1L); phone.addUsage(date, 12);` L'historique conserve ses données par identifiant ; l'abonné n'a donc pas besoin de contenir les sessions lui-même.
