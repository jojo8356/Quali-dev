# « Avant de coder, penser SOLID » : ce qui existe vraiment en entreprise

## 1. La réponse courte

**Non, il n'existe aucun papier officiel qui impose SOLID avant de coder.**

- SOLID n'est **pas une norme** (ni ISO, ni IEEE, ni AFNOR), ni une obligation
  légale. C'est une **heuristique de conception** popularisée par Robert C. Martin
  (articles 1996-2000, livre *Clean Architecture*, 2017). Personne ne peut te
  « certifier SOLID ».
- Les normes qui existent réellement portent sur **ce qu'il faut documenter**,
  pas sur *comment* concevoir :
  - **ISO/IEC/IEEE 42010:2022** — *Architecture description* : exige qu'une
    description d'architecture identifie les parties prenantes, leurs
    préoccupations, les points de vue et les **décisions** ;
  - **IEEE 1016-2009** — *Software Design Descriptions* : contenu et
    organisation d'un document de conception (vues + **justification** ;
    statut « Inactive-Reserved » depuis 2020, mais toujours citée) ;
  - **ISO/IEC 25010:2023** — modèle de qualité produit : la **maintenabilité**
    (modularité, réutilisabilité, analysabilité, modifiabilité, testabilité) est
    une caractéristique mesurable. C'est l'argument « business » de SOLID.
- Ce qui **fait office de papier officiel** en entreprise, ce sont des artefacts
  d'équipe : *Definition of Ready*, *Definition of Done*, *design doc*, *ADR*,
  revue de conception, modèle de *pull request*, règles de qualité automatisées.
  Aucun n'est universel : chacun est **local à l'entreprise**.

Retiens la formule : **SOLID n'est pas une norme, c'est une convention d'équipe
— et une convention n'existe que si elle est écrite et vérifiée quelque part.**

---

## 2. La chaîne d'artefacts (du ticket au code)

C'est cette chaîne, et pas un document unique, qui fait qu'on « pense SOLID »
automatiquement.

| Moment | Artefact | Ce qu'il contient | Lien avec SOLID |
|---|---|---|---|
| Avant de prendre le ticket | **Definition of Ready** | critères d'acceptation, contraintes, maquettes, dépendances | force à se demander *ce qui va changer* (OCP) et *qui utilise quoi* (ISP) |
| Conception (30-60 min) | **Design note / Tech spec / RFC** | contexte, options, diagramme, classes/interfaces, impacts | le seul endroit où les 5 principes se discutent vraiment |
| Décision structurante | **ADR** (*Architecture Decision Record*) | contexte, décision, conséquences, alternatives | trace *pourquoi* on a découpé ainsi → évite la re-discussion |
| Revues | **Design review** (30 min, 2-3 personnes) | questions, risques, alternatives | détecte les contrats trop gros avant d'écrire du code |
| Avant de merger | **Pull request template** | checklist (contrats, tests, doc) | rattrape ce qui a échappé |
| En continu | **Quality gate** (SonarQube, ArchUnit, Checkstyle) | seuils + tests d'architecture | rend SOLID **vérifiable**, pas seulement « conseillé » |
| Fin de ticket | **Definition of Done** | tests, doc, ADR si décision, *boy scout rule* | évite que la dette reparte |

Dans la vraie vie, une équipe mature en utilise **3 ou 4**, pas 7. Les plus
fréquents : *Definition of Done* + modèle de PR + une revue de conception
légère. Les plus rares : design doc complet (réservé aux sujets structurants).

---

## 3. Le document à créer : « Design note SOLID » (1 page)

À joindre au ticket (Jira/Linear/GitHub issue) ou en commentaire de PR. C'est
**la** réponse concrète à ta question : un canevas que l'équipe remplit
systématiquement, et qui contient une question par principe.

```markdown
## Design note — <TICKET-123 : titre>

### 1. Besoin
En une phrase : qui a besoin de quoi, et pourquoi maintenant.
Critères d'acceptation : 2 à 4 puces testables.

### 2. Ce qui risque de changer (OCP)
- Variations déjà prévues : <ex. TVA, moyens de paiement, format d'export>
- Variations probables à 6 mois : <...>
- → Point d'extension choisi : <interface / stratégie / paramètre>
- Ce que je N'étends PAS maintenant (YAGNI) : <...>

### 3. Découpage des responsabilités (SRP)
| Classe / module | Une seule raison de changer ? | Responsabilité |
|---|---|---|
| OrderService | oui | orchestration |
| OrderPricing | oui | calcul des montants |

### 4. Contrats (ISP)
- Interfaces créées / modifiées : <nom + 1 phrase>
- Qui l'implémente ? Qui l'appelle ?
- Y a-t-il une méthode qu'un implémenteur n'utilisera jamais ? <oui/non>
- Si oui → découpage prévu : <...>

### 5. Substitution (LSP)
- Sous-types prévus : <...>
- Promesses que TOUT sous-type doit tenir : <préconditions, postconditions, exceptions>
- Cas où un sous-type « casserait » l'appelant : <...>

### 6. Dépendances (DIP)
- Le domaine dépend-il d'un détail technique (BDD, HTTP, framework) ? <oui/non>
- Interface placée côté domaine : <...>
- Implémentation concrète : <...>
- Comment je teste sans la vraie BDD ? <fake / mock / in-memory>

### 7. Risques et alternatives
- Option A (choisie) / Option B (rejetée, pourquoi)
- Dette assumée + ticket de suivi : <...>
```

**Règle d'or :** si le canevas prend plus de 15 minutes, c'est que le ticket est
trop gros ou trop flou — pas que le canevas est trop long.

---

## 4. Les deux checklists qui rendent ça automatique

### 4.1 Definition of Ready (avant de commencer)

- [ ] Le besoin est exprimé en terme de **comportement observable** (pas de
      solution technique imposée : « ajouter un pattern Strategy » n'est pas un
      besoin).
- [ ] Critères d'acceptation écrits et testables.
- [ ] On sait **qui consomme** le résultat (autre équipe ? API publique ? UI ?).
- [ ] On a identifié si le ticket **touche un contrat partagé** (interface
      publique, schéma de BDD, API). Si oui → design note obligatoire.
- [ ] Un « spike » (exploration timeboxée) est prévu si l'inconnu est technique.

### 4.2 Definition of Done (avant de fermer)

- [ ] Tests automatisés sur le nouveau comportement.
- [ ] Aucune méthode implémentée « pour rien » (`null`, vide,
      `UnsupportedOperationException`) → signal ISP.
- [ ] Aucun `new` d'un détail d'infrastructure dans le domaine → signal DIP.
- [ ] Le contrat public a-t-il changé ? Si oui : versionnement + doc + ADR.
- [ ] **Boy scout rule** : on laisse le fichier un peu plus propre qu'en arrivant.
- [ ] Si une décision structurante a été prise → **ADR ajouté**.
- [ ] Dette assumée → ticket créé, sinon elle est invisible.

---

## 5. Le modèle d'ADR (format Nygard, 2011 — le plus utilisé)

Fichier `docs/adr/0007-segregation-des-services-de-persistance.md` :

```markdown
# ADR 0007 — Séparer findByName du contrat de persistance

## Statut
Accepté (2026-10-21) | Remplacé par ADR-XXXX | Déprécié

## Contexte
PersistenceService<T> imposait findByName à toutes les entités. Order n'a pas
de nom : l'implémentation était obligée de mentir (null). Toute nouvelle entité
sans nom héritait du même problème.

## Décision
Nous séparons le contrat en deux :
- PersistenceService<T> : save / delete / findById
- NameSearchablePersistenceService<T> extends PersistenceService<T> : + findByName
UserPersistenceService implémente la seconde, OrderPersistenceService la première.

## Conséquences
Positif : erreur détectée à la compilation, plus de méthode morte.
Négatif : deux interfaces à connaître ; les clients qui veulent findByName
doivent déclarer le type précis (pas PersistenceService<T>).

## Alternatives écartées
- return null dans OrderPersistenceService : mensonge sur le contrat.
- UnsupportedOperationException : erreur repoussée à l'exécution.
- findByName sur Entity : déplace le problème sur toutes les entités.
```

Un ADR = **1 page max**, jamais modifié après acceptation (on le remplace). Il
vit **dans le dépôt Git**, à côté du code : c'est ça qui le rend « officiel »
pour l'équipe, pas une norme externe.

---

## 6. Rendre SOLID vérifiable (la partie que les équipes oublient)

Un document sans contrôle est un vœu pieux. Deux mécanismes concrets :

### 6.1 Test d'architecture avec ArchUnit (Java)

```java
// dépendance de test : com.tngtech.archunit:archunit-junit5
@AnalyzeClasses(packages = "com.company")
class ArchitectureTest {

  @ArchTest
  static final ArchRule le_domaine_ne_depend_pas_de_l_infrastructure =
      noClasses().that().resideInAPackage("..domain..")
          .should().dependOnClassesThat().resideInAnyPackage("..jdbc..", "..http..");

  @ArchTest
  static final ArchRule pas_de_methode_morte_dans_les_services =
      noClasses().should().callMethodWhere(target ->
          target.getOwner().isAssignableFrom(org.junit.jupiter.api.Disabled.class));
}
```

Les règles vraiment utiles à écrire dans un premier temps :
- le domaine ne dépend pas de l'infrastructure (= DIP) ;
- aucune classe du package `service` n'implémente `findByName` sans être dans
  une liste blanche (= ISP) ;
- pas de dépendance cyclique entre packages (= SRP mal découpé).

### 6.2 Quality gate

| Outil | Ce qu'il attrape | Limite |
|---|---|---|
| SonarQube | duplication, complexité, code mort, exceptions avalées | ne comprend pas l'intention |
| Checkstyle / PMD | style, imports inutilisés, `switch` non exhaustif | bruit si mal configuré |
| ArchUnit / jQAssistant | règles d'architecture réelles | à écrire et maintenir |
| Couverture de tests | zones non testées | 100 % ≠ bon design |

**Aucun outil ne vérifie « c'est bien SOLID ».** Ils vérifient des **symptômes**
(dette, couplage, code mort). D'où le binôme : revue humaine + règles
automatiques.

---

## 7. La réalité du terrain (à savoir avant d'en parler en entretien)

- **La majorité des équipes n'ont pas de design note SOLID.** Elles ont des
  revues de code, des tests, et parfois un ADR pour les gros sujets. C'est
  suffisant dans 80 % des cas.
- **Les grosses structures** (banque, assurance, ESN, industrie) utilisent des
  documents plus lourds : dossier de conception détaillé, revue d'architecture
  en comité, traçabilité vers les exigences (héritage ISO/IEEE 1016 / 42010).
  Là, SOLID apparaît souvent comme une **grille de relecture**, pas comme un
  chapitre obligatoire.
- **Le piège inverse existe** : le dogmatisme SOLID (une interface par méthode,
  des couches abstraites partout, « on ne sait pas ce que ça fait mais c'est
  SOLID ») coûte cher et se voit en revue. La bonne pratique est **YAGNI** :
  appliquer SOLID au moment où le changement devient probable, pas avant.
- **Vocabulaire d'entretien** qui marche : « on fait une design review de 30
  minutes quand le ticket touche un contrat partagé, on trace les décisions en
  ADR, et on a des tests ArchUnit qui empêchent le domaine de dépendre de
  l'infra ». C'est concret, vérifiable, et ça montre que tu sais que SOLID est
  une **convention**, pas une norme.

---

## 8. Résumé en 5 lignes

1. Aucun papier officiel n'impose SOLID : ce n'est ni une norme ISO/IEEE, ni une
   obligation du Scrum Guide.
2. Les normes existantes encadrent la **documentation** de conception et la
   **qualité** (ISO/IEC/IEEE 42010:2022, IEEE 1016-2009, ISO/IEC 25010:2023).
3. Ce qui rend SOLID « automatique » en entreprise, c'est une **chaîne** :
   Definition of Ready → design note d'une page → ADR → revue → checklist de PR
   → quality gate.
4. Une question par principe dans le canevas suffit (SRP : qui change pourquoi ?
   OCP : qu'est-ce qui varie ? LSP : quelles promesses ? ISP : qui n'utilise
   pas cette méthode ? DIP : le domaine dépend-il d'un détail ?).
5. Sans vérification automatique ni revue, le document reste un vœu : c'est la
   **revue** et les **tests d'architecture** qui font respecter le contrat.

## 9. Références

- ISO/IEC/IEEE 42010:2022 — *Software, systems and enterprise — Architecture
  description* (2ᵉ édition, 2022-11).
- IEEE 1016-2009 — *Software Design Descriptions* (statut Inactive-Reserved).
- ISO/IEC 25010:2023 — *Product quality model* (remplace 25010:2011 ;
  maintenabilité = modularité, réutilisabilité, analysabilité, modifiabilité,
  testabilité).
- Michael Nygard, *Documenting Architecture Decisions*, 15 nov. 2011 —
  https://www.cognitect.com/blog/2011/11/15/documenting-architecture-decisions
- Modèles ADR : https://adr.github.io/adr-templates/
- Robert C. Martin, *Agile Software Development: Principles, Patterns, and
  Practices* (2002) et *Clean Architecture* (2017).
- ArchUnit : https://www.archunit.org/
