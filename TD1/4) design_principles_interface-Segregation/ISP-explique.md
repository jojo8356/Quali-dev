# ISP — Interface Segregation Principle, expliqué simplement

> **En une phrase :** personne ne doit être obligé de promettre de savoir faire
> un truc dont il n'aura jamais besoin.

ISP = le **I** de SOLID. « Segregation » = séparation, découpage.

---

## 1. L'image du cartable (version enfant)

Imagine un cartable magique. Chaque matin, il se remplit **tout seul** avec
**tous les livres de toute l'école** : les maths, l'histoire, la chimie des
grands, le livre de conduite, le dictionnaire de japonais…

- Tu as 9 ans. Tu vas en classe le lundi avec 30 kg sur le dos.
- Tu n'utilises vraiment que **3 livres**.
- Mais tu es **obligé de les porter tous**, et si un livre est déchiré, c'est
  *toi* qui es en faute.

**La solution ISP :** au lieu d'un cartable-fourre-tout, on regarde **ce dont
chaque journée a besoin** et on prépare **des petits cartables** :
un pour les jours de maths, un pour les jours de sport, etc. Chacun ne porte
que ce qu'il utilise.

En programmation :

| Le cartable | L'interface (`interface` en Java) |
|---|---|
| Les livres | Les méthodes (`save`, `delete`, `findByName`…) |
| « Je porte le cartable » | « J'implémente l'interface » (`implements`) |
| Les 30 kg inutiles | Les méthodes qu'on est forcé d'écrire et qu'on n'utilisera jamais |

Une interface, c'est une **liste de promesses** : « je sais faire ceci, je sais
faire cela ». ISP dit : **ne me fais pas promettre des choses inutiles**.

---

## 2. La définition « vraie », mais en clair

> *Les clients ne doivent pas être forcés de dépendre d'interfaces qu'ils
> n'utilisent pas.*

Traduction :

- Une **interface trop grosse** = un menu unique à la cantine où tout est
  mélangé : tout le monde subit des plats qu'il ne mange pas.
- Une **interface découpée** = un self-service : chacun prend ce qu'il veut.

Le point important : ISP parle des **clients** (ceux qui *appellent* l'interface)
autant que des **implémentations** (ceux qui *écrivent* le code derrière). Si une
classe `A` n'utilise que 2 méthodes sur 10, elle dépend quand même des 10 : si on
change une des 8 autres, il faut recompiler/retester `A` pour rien.

---

## 3. Comment repérer une violation (les 4 signaux)

Tu vois l'un de ces signes dans ton code ? C'est très probablement une violation
d'ISP.

1. **La méthode morte** : une classe implémente une méthode et la laisse vide,
   ou fait `return null;`, ou lance `throw new UnsupportedOperationException();`.
2. **Le commentaire d'excuse** : `// pas de nom pour une commande, donc on ne
   fait rien ici`.
3. **Les paramètres fantômes** : une méthode reçoit un argument qu'elle
   n'utilise jamais (ou une classe a un champ qu'elle ne lit jamais).
4. **Deux publics, une seule interface** : la moitié des méthodes ne sert qu'à
   un groupe de classes, l'autre moitié à un autre groupe.

---

## 4. La recette pour corriger (4 étapes)

1. **Étaler le contrat.** Écris l'interface et, à côté de chaque méthode, deux
   colonnes : *qui l'implémente vraiment ?* / *qui l'appelle ?*
2. **Regrouper par besoin**, pas par méthode. Les méthodes utilisées par les
   mêmes personnes vont ensemble (ex. : « CRUD de base » / « recherche par
   nom » / « export CSV »).
3. **Créer plusieurs petites interfaces.** Deux styles possibles :
   - une interface de base + des interfaces « capacité » qui l'étendent :
     `interface NameSearchable<T> extends PersistenceService<T>`;
   - ou des interfaces totalement indépendantes, et la classe en implémente
     plusieurs : `class UserService implements Saveable<User>, Searchable<User>`.
4. **Faire dépendre chaque client de la plus petite interface qui lui suffit.**
   Un client qui veut juste enregistrer reçoit `Saveable`, pas le contrat géant.

Règle de pouce pour le découpage : **on découpe par « qui en a besoin », jamais
par « ça fait joli, une méthode par interface »** (voir §6).

---

## 5. Application à notre TP 4 (`TD1/4) … interface-Segregation`)

### Avant — le cartable de 30 kg

```java
public interface PersistenceService<T extends Entity> {
  void save(T entity);
  void delete(T entity);
  T findById(Long id);
  List<T> findByName(String name);   // <-- le livre de trop
}
```

- `User` a un nom → `findByName` a du sens.
- `Order` **n'a pas de nom** (elle a une date et un montant) → `findByName`
  n'a **aucun** sens.
- Conséquence : `OrderPersistenceService` devait quand même écrire cette
  méthode. Le seul choix possible était de mentir : `return null;` ou lever une
  exception. C'est exactement le cartable de 30 kg.

### Après — deux petits cartables

```java
// Le contrat minimal : vrai pour TOUTES les entités.
public interface PersistenceService<T extends Entity> {
  void save(T entity);
  void delete(T entity);
  T findById(Long id);
}

// La capacité en plus : seulement pour les entités qui ont un nom.
public interface NameSearchablePersistenceService<T extends Entity>
        extends PersistenceService<T> {
  List<T> findByName(String name);
}
```

```java
public class OrderPersistenceService implements PersistenceService<Order> {
  // save / delete / findById uniquement. Rien d'inutile, rien à mentir.
}

public class UserPersistenceService
        implements NameSearchablePersistenceService<User> {
  // save / delete / findById / findByName
}
```

Le bénéfice concret, en une ligne de `Main` :

```java
PersistenceService<Order> orderService = new OrderPersistenceService();
orderService.findByName("Ada");   // <-- ne compile même pas : et c'est bien !
```

L'erreur est attrapée par le **compilateur** au lieu d'exploser à l'exécution
(`UnsupportedOperationException`). C'est ça, la valeur d'ISP : le contrat dit la
vérité, donc les erreurs se voient tôt.

Et `UserPersistenceService` **reste** un `PersistenceService<User>` (grâce à
`extends`) : partout où l'on n'a besoin que du CRUD, on peut le passer sans rien
changer. ISP ne casse pas l'interchangeabilité, il la précise.

---

## 6. Les fausses solutions (à ne pas faire)

| Fausse solution | Pourquoi c'est raté |
|---|---|
| `OrderPersistenceService.findByName()` qui `return null;` | On a « respecté » l'interface en **mentant**. Le client croit que la recherche marche, il reçoit `null` et plante plus loin. |
| `throw new UnsupportedOperationException();` | Même problème, version bruyante : le bug arrive à l'exécution au lieu de la compilation. |
| Mettre `findByName` dans `Entity` | On déplace le problème : `Order` hérite d'une méthode absurde. |
| Tout mettre dans une seule interface « pour faire simple » | C'est le cartable de 30 kg. « Simple » au début, pénible à faire évoluer. |
| Une interface par méthode, partout | ISP mal dosé : 40 interfaces d'une méthode, on ne comprend plus rien. On découpe par **besoin d'un client**, pas au hasard. |
| Faire `if (entity instanceof User)` dans une méthode générique | On remplace un problème de contrat par un problème de `instanceof` : le code ne sait plus de quoi il parle. |

---

## 7. Le même principe, déjà présent dans notre TD5

```java
public interface Formatter {          // 1 seule méthode, 1 seul besoin
  String format(Message message) throws FormatException;
}
```

`JSONFormatter` et `TextFormatter` l'implémentent ; `MessagePrinter` ne dépend
que de ce minuscule contrat (pas de `JSONFormatter`). Résultat : on peut ajouter
un `XmlFormatter` sans toucher à `MessagePrinter`. C'est un bon découpage ISP —
petit, stable, centré sur un besoin.

À l'inverse, si `Formatter` contenait aussi `formatCSV()`, `sendByMail()` et
`saveToDatabase()`, chaque formateur devrait écrire trois méthodes inutiles.

---

## 8. Checklist avant de rendre un TP

- [ ] Aucune méthode implémentée « pour rien » (vide, `null`, exception).
- [ ] Aucun commentaire du type « pas supporté pour cette classe ».
- [ ] Chaque interface porte un **nom qui décrit un besoin** (`NameSearchable`,
      `Formatter`), pas un nom fourre-tout (`Manager`, `Helper`, `Service`).
- [ ] Chaque client (méthode qui reçoit l'interface en paramètre) ne voit que
      les méthodes dont il se sert.
- [ ] Si j'ajoute une nouvelle entité (ex. `Invoice` sans nom), je n'ai **rien**
      à réécrire de faux : je choisis juste la bonne interface.

---

## 9. Mini-quiz (3 questions)

1. `interface Animal { void manger(); void voler(); }` avec un chien et un
   poisson : qui viole ISP, et comment découper ?
2. `OrderPersistenceService` doit-il implémenter `PersistenceService<Order>`,
   `NameSearchablePersistenceService<Order>`, ou les deux ? Pourquoi ?
3. Un collègue dit : « ISP = une méthode par interface ». Vrai ou faux ?

<details>
<summary>Réponses</summary>

1. `Animal` viole ISP : `Poisson` est obligé d'écrire `voler()` pour rien.
   On découpe : `Mangeable` / `Volant`, le chien implémente les deux, le poisson
   seulement `Mangeable`.
2. Uniquement `PersistenceService<Order>` : `Order` n'a pas de nom, donc
   `findByName` n'a pas de sens pour elle.
3. Faux. On découpe par **besoin du client**, pas par nombre de méthodes. Une
   interface de 5 méthodes utilisées ensemble par les mêmes clients est très
   bien.

</details>
