# `extends` en Java — et pourquoi il n'y a pas de `super()` dans notre TP

## 0. Le piège : `extends` ne veut pas dire la même chose selon ce qu'il y a à gauche

C'est la source de 90 % des confusions.

| Ce qu'il y a à gauche | Ce que `extends` veut dire | Constructeur ? | `super()` possible ? |
|---|---|---|---|
| `class Fille extends Mere` | **Héritage** : je récupère le **code** (champs + méthodes) de `Mere` | Oui | **Oui** (obligatoire si la mère n'a pas de constructeur vide) |
| `interface B extends A` | **Fusion de contrats** : mes promesses = les miennes + celles de `A` | **Non, jamais** | **Non, jamais** |
| `class X implements I` | Je **promets** de savoir faire ce que dit `I` | Oui (le sien) | Oui, mais pour **sa** mère, pas pour `I` |
| `<T extends Entity>` ou `List<? extends Number>` | **Borne de généricité** : « T est un `Entity` ou une sous-classe » | — | — |

Dans notre TP 4 :

```java
public interface NameSearchablePersistenceService<T extends Entity>
        extends PersistenceService<T> { ... }
```

Il y a **deux** `extends` sur cette ligne, et ils ne veulent pas dire la même chose :

- `T extends Entity` → borne : « T doit être un `Entity` (ou mieux) ».
- `... extends PersistenceService<T>` → la partie gauche est une **interface** :
  je récupère ses **méthodes abstraites**, pas son code. Donc **aucun
  constructeur, aucun `super()`**. Il n'y a rien à initialiser : une interface
  n'a **aucun état** (aucun champ d'instance).

---

## 1. `extends` sur une classe : qu'est-ce qui est hérité ?

```java
class Mere {
  private   String secret  = "privé";     // hérité en mémoire, mais pas accessible
  protected String maison  = "Nice";
  public    String nom     = "Maman";

  public void direBonjour() { System.out.println("Bonjour de " + nom); }
}

class Fille extends Mere {
  public void test() {
    // System.out.println(secret);  // ❌ private : invisible, même pour la fille
    System.out.println(maison);     // ✅ protected : OK
    direBonjour();                  // ✅ méthode héritée : OK
  }
}
```

Ce qui est hérité :

- ✅ les **champs** (même `private` : ils existent dans l'objet, on n'y a juste pas accès) ;
- ✅ les **méthodes** accessibles (`public`, `protected`, package si même package) ;
- ✅ le **type** : une `Fille` **est une** `Mere` → on peut écrire `Mere m = new Fille();`.

Ce qui n'est **jamais** hérité :

- ❌ les **constructeurs** (on ne peut pas faire `new Fille()` en appelant le
  constructeur de `Mere` comme une méthode) ;
- ❌ les champs/méthodes `private` **en accès direct** ;
- ❌ rien du tout si la classe mère est `final` (interdit d'hériter).

Une seule mère possible : **`class A extends B` est unique**. Le multiple, c'est
pour les interfaces (`class A implements X, Y, Z`).

Et toute classe sans `extends` hérite implicitement de **`Object`** — c'est pour
ça qu'un `super()` a toujours quelque chose à appeler : `Object` a un
constructeur vide.

---

## 2. À quoi ça sert vraiment (le but, pas la syntaxe)

1. **Éviter la copie** : le code commun vit une seule fois dans la mère.
2. **La substitution** : partout où on attend une `Mere`, on peut mettre une
   `Fille` → c'est ce qui permet le polymorphisme :

```java
PersistenceService<Order> service = new OrderPersistenceService(); // ← notre TP
service.save(order); // la version de OrderPersistenceService est appelée
```

Le compilateur ne connaît que le contrat (`PersistenceService`), l'exécution
choisit l'implémentation réelle. C'est **le** mécanisme derrière SOLID (et
derrière LSP, le TP 3 : la fille doit tenir les promesses de la mère).

3. **Redéfinir** (`override`) un comportement :

```java
class Fille extends Mere {
  @Override
  public void direBonjour() { System.out.println("Salut, moi c'est " + nom); }
}
```

Attention : `extends` **crée un couplage fort**. Si tu changes la mère, tu peux
casser les filles. D'où la règle : *hériter pour « est un »*, pas pour
« réutiliser du code au rabais » (là, préférer la **composition** : un champ).

---

## 3. `super()` : ce n'est pas « optionnel », c'est **invisible**

La question « comment la mère est initialisée si je n'appelle pas `super()` ? »
a une réponse simple : **Java l'appelle pour toi**. Toujours.

```java
class Mere {
  String nom = "Maman";
  Mere() { System.out.println("Mere(), nom=" + nom); }
}

class Fille extends Mere {
  String prenom = "Léa";
  Fille() { System.out.println("Fille(), prenom=" + prenom); }
}

new Fille();
```

Ce que le compilateur lit réellement :

```java
Fille() {
  super();  // ← INSÉRÉ automatiquement, en première instruction
  System.out.println("Fille(), prenom=" + prenom);
}
```

Ordre d'exécution (à connaître par cœur) :

1. les blocs `static` de `Mere` puis de `Fille` (une seule fois, au chargement) ;
2. initialisation des **champs de `Mere`** (`nom = "Maman"`) ;
3. corps du **constructeur de `Mere`** ;
4. initialisation des **champs de `Fille`** (`prenom = "Léa"`) ;
5. corps du **constructeur de `Fille`**.

Sortie :

```
Mere(), nom=Maman
Fille(), prenom=Léa
```

**Il n'existe aucun moyen d'éviter l'initialisation de la mère.** On ne peut
même pas mettre du code avant `super()` : `super(...)` doit être la **première
instruction** du constructeur.

### Quand `super(...)` devient obligatoire (et l'erreur qui va avec)

L'appel implicite ne marche que si la mère a un constructeur **sans argument**.
Sinon → erreur de compilation :

```java
class Mere {
  Mere(String nom) { }        // pas de constructeur vide
}

class Fille extends Mere {
  Fille() { }                 // ❌ error: constructor Mere in class Mere
                              //    cannot be applied to given types
}
```

Correction : soit on écrit l'appel explicite, soit on utilise `this(...)`, soit
on ajoute un constructeur vide à la mère.

```java
class Fille extends Mere {
  Fille() {
    super("Léa");             // ✅ première instruction
  }
}
```

### Les trois formes d'appel à un constructeur

```java
class Fille extends Mere {
  int age;

  Fille()            { super("Léa"); age = 10; }  // constructeur de la MÈRE
  Fille(int age)     { this();      this.age = age; }
  //                  └ constructeur de LA MÊME classe ; il finira par appeler super()
}
```

`super(...)` et `this(...)` sont **exclusifs** et doivent être en **première
ligne**. Un constructeur finit donc **toujours** par atteindre `super()` de
`Object` — soit directement, soit en remontant la chaîne.

---

## 4. Pourquoi **aucun** `super()` dans `OrderPersistenceService` ni dans nos interfaces

```java
public class OrderPersistenceService implements PersistenceService<Order> { ... }
```

Deux choses à réaliser :

1. **`OrderPersistenceService` n'hérite pas de `Order`.** Il n'y a pas de
   `extends` vers une classe. `<Order>` n'est qu'un **paramètre de type** : il
   dit « ce service sait stocker des `Order` », pas « je suis une `Order` ».
   La seule mère est `Object`, dont le constructeur est vide → `super()`
   implicite, invisible, et parfaitement suffisant.
2. **`PersistenceService` est une interface** : aucune donnée à initialiser.
   Ses méthodes n'ont pas de corps à récupérer — ce sont des **promesses**.

Et dans l'interface :

```java
public interface NameSearchablePersistenceService<T extends Entity>
        extends PersistenceService<T> {
  List<T> findByName(String name);
}
```

- `extends PersistenceService<T>` = « mon contrat **contient aussi** `save`,
  `delete`, `findById` ».
- Une interface peut `extends` **plusieurs** interfaces
  (`interface A extends B, C`) : c'est de l'héritage **multiple de type**, sans
  le bazar de l'héritage multiple de code (pas de conflit d'état, puisqu'il n'y
  a pas d'état).
- Une classe qui implémente la fille doit fournir **toutes** les méthodes de la
  fille **et** de la mère :

```java
class UserPersistenceService implements NameSearchablePersistenceService<User> {
  // doit écrire : save, delete, findById (héritées du contrat) + findByName
}
```

- Dépuis Java 8 (`<maven.compiler.source>1.8</maven.compiler.source>` dans notre
  `pom.xml`), une interface peut avoir des méthodes `default` (avec un corps) et
  `static`. Mais **toujours aucun constructeur** : une interface n'est jamais
  instanciée.

---

## 4 bis. Cas concret : `class Order extends Entity` n'écrit aucune méthode de `Entity`

Notre code :

```java
// entity/Entity.java
public abstract class Entity {
  private Long id;
  public Long getId()            { return id; }
  public void setId(Long id)     { this.id = id; }
}

// entity/Order.java
public class Order extends Entity {
  private LocalDateTime orderPlacedOn;
  private double totalValue;

  // ← AUCUN getId() / setId() écrit ici, et pourtant :
}
```

```java
Order order = new Order();
order.setId(1L);                 // ✅ fonctionne : la méthode vient de Entity
Long id = order.getId();         // ✅ idem
```

Pourquoi ça marche sans réécrire quoi que ce soit :

### a) Le code est **hérité**, il n'est pas recopié

`extends Entity` dit : « une `Order` **est une** `Entity` », donc elle **possède
déjà** le champ `id` et les méthodes `getId` / `setId`. Le compilateur les
cherche dans `Order`, ne les trouve pas, remonte chez `Entity`, les trouve et
les utilise. Rien à déclarer : **hériter = recevoir, pas réécrire**.

En mémoire, un objet `Order` ressemble à :

```
[ partie Entity : id = 1 ]   ← héritée, mais privée (accès via get/set uniquement)
[ partie Order  : orderPlacedOn, totalValue ]
```

### b) La différence de fond avec `implements` : **y a-t-il un corps ?**

| Ce que je « reçois » | Du code exécutable ? | Dois-je le réécrire ? |
|---|---|---|
| Méthode **concrète** d'une classe mère (`getId() { return id; }`) | **Oui** | ❌ jamais |
| Méthode **`default`** d'une interface (Java 8+) | Oui | ❌ optionnel |
| Méthode **abstraite** d'une interface (`void save(T e);`) | **Non**, juste une promesse | ✅ **obligatoire** |
| Méthode **`abstract`** d'une classe mère abstraite | Non | ✅ **obligatoire** |

C'est **toute** la nuance qui répond à ta question :

- `class Order extends Entity` → `Entity` fournit un **corps** pour `getId`/
  `setId`, donc `Order` n'a rien à faire.
- `class OrderPersistenceService implements PersistenceService<Order>` →
  l'interface ne fournit **aucun corps**, seulement des signatures : la classe
  **doit** écrire `save`, `delete`, `findById`. Sinon, erreur de compilation :
  *« OrderPersistenceService is not abstract and does not override abstract
  method save(Order) »*.

Vérifions avec le seul cas où `Order` **devrait** écrire quelque chose :

```java
public abstract class Entity {
  private Long id;
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public abstract String label();   // ← pas de corps : promesse seule
}

public class Order extends Entity {
  // ❌ erreur : Order is not abstract and does not override abstract method label()
  // ✅ il faudrait : public String label() { return "Order #" + getId(); }
}
```

`abstract class Entity` (notre cas) ne change rien à l'héritage du code : le mot
`abstract` interdit seulement `new Entity()` — on ne veut pas d'entité « toute
seule », seulement des `User` et des `Order`.

### c) On peut quand même **redéfinir** une méthode héritée, mais c'est un choix

```java
public class Order extends Entity {
  @Override
  public String toString() { return "Order #" + getId(); }  // override de Object
}
```

- Redéfinir (`override`) = **changer le comportement** ; c'est facultatif et
  seulement quand on a une raison.
- `@Override` n'est pas obligatoire, mais il fait **vérifier par le
  compilateur** qu'une méthode de même signature existe bien dans la mère : ça
  évite les fautes de frappe silencieuses (`getID()` ≠ `getId()`).
- Les **constructeurs**, eux, ne sont **jamais** hérités (voir §3) : `Order` a
  son constructeur implicite `Order()`, qui appelle `super()` → `Entity()` →
  `Object()`.

### d) Le lien avec ISP (pour ne pas mélanger les deux sujets)

- `extends Entity` = **réutilisation de code** : je reçois `id` et son accès.
- `implements PersistenceService` = **contrat** : je promets des méthodes, je les
  écris moi-même.

Si `Entity` contenait 15 méthodes concrètes dont 12 inutiles à `Order`, on ne
violerait **pas** ISP au sens strict (le code est reçu, pas écrit), mais on
aurait un **héritage trop large** : `Order` porterait un sac à dos de
comportements dont elle ne veut pas. La réponse est la même qu'en ISP : **on
découpe** — soit en sortant ces méthodes dans une classe à part (composition),
soit en les mettant dans des interfaces que seule la classe concernée implémente.

---

## 4 ter. « Comment il peut `setId` alors qu'on n'a pas déclaré d'`id` ? »

### a) Le champ **est** déclaré — mais chez la mère

```java
public abstract class Entity {
  private Long id;                       // ← LE champ, déclaré ICI
  public Long getId()      { return id; }
  public void setId(Long i) { id = i; }  // ← LE code qui l'écrit, ICI
}

public class Order extends Entity {
  private LocalDateTime orderPlacedOn;   // rien sur id : c'est normal
  private double totalValue;
}

Order order = new Order();
order.setId(1L);      // ✅ on n'écrit pas dans Order, on écrit dans sa PARTIE Entity
```

Un objet `Order` n'est pas « un objet `Order` + un objet `Entity` à côté » : c'est
**un seul** objet qui contient **les deux zones** :

```
instance de Order en mémoire
┌──────────────────────────────────────────┐
│ (zone héritée de Entity) id : null → 1   │  ← setId écrit ici
├──────────────────────────────────────────┤
│ orderPlacedOn : 2026-10-21T10:00          │
│ totalValue    : 149.9                    │
└──────────────────────────────────────────┘
```

Le champ `id` existe donc **une fois par instance de `Order`** (une seule), et
`order.setId(1L)` le remplit. Ce que tu n'as pas écrit, c'est le **champ** ; ce
que le compilateur fait, c'est **remonter** la chaîne des classes pour trouver
la méthode `setId` — c'est l'héritage.

Vérifiable en une ligne : `javap -p -c com.company.intersegrega.entity.Order`
montre **zéro** champ `id` et **aucune** méthode `setId` dans la classe `Order`.
Elles sont dans `Entity.class`. À l'exécution, la méthode `Entity.setId` tourne
avec `this` = l'instance `Order` : elle écrit dans la zone `id` **de cet objet-là**
(pas dans un objet `Entity` séparé).

### b) Et `private` n'empêche pas ça ?

Non, parce que `setId` **est du code de `Entity`**, et `private` signifie
« accessible **depuis la classe où le champ est déclaré** », pas « depuis
l'objet ». Même si l'objet réel est une `Order`, la méthode qui s'exécute
appartient à `Entity`, qui a le droit de toucher son propre `private`.

```java
public class Main {
  public static void main(String[] args) {
    Order order = new Order();
    // order.id = 1L;   // ❌ id is not public in Entity; cannot be accessed from outside package
    order.setId(1L);    // ✅ passer par le setter public
  }
}
```

**Si `Entity` n'avait pas de setter** (champ `private` seul), alors **personne**
ne pourrait renseigner `id` de l'extérieur : il resterait `null` pour toujours,
sauf via le constructeur de `Entity`. C'est pour ça qu'un champ « technique »
comme l'id a un getter *et* un setter.

### c) `Long` n'est pas un `long` : c'est un objet (une « boîte »)

```java
private Long id;   // référence vers un objet Long → vaut null tant qu'on n'a rien mis
```

- `long` (minuscule) = type primitif, une valeur, jamais `null`, vaut `0` par défaut.
- `Long` (majuscule) = **classe** (wrapper), donc `id` est une **référence** vers
  un objet ; sa valeur par défaut est **`null`**, pas `0`.
- `setId(1L)` : `1L` est un `long`. Java fait une **autoboxing** implicite :
  `Long.valueOf(1L)` fabrique l'objet, puis la référence est stockée dans `id`.
- `getId()` **avant** tout `setId` renvoie donc `null` → attention si on s'en sert
  comme clé de `HashMap` : deux `Order` sans id s'écrasent mutuellement
  (clé `null` unique), et `ORDERS.put(null, ...)` ne plantera pas mais perdra
  des données. D'où l'importance de `setId` avant `save` :

```java
Order order = new Order();
order.setTotalValue(149.90);
orderService.save(order);      // ⚠️ id == null : une seule clé null dans la map
order.setId(1L);
orderService.save(order);      // ✅ clé 1L
```

### d) Rappel : `extends` donne le champ, `implements` ne donne rien

| | `class Order extends Entity` | `class X implements PersistenceService<Order>` |
|---|---|---|
| Le champ `id` existe dans l'objet ? | **oui**, hérité de `Entity` | non (pas d'état dans une interface) |
| Le code de `setId` existe ? | **oui**, hérité | non |
| Dois-je écrire les méthodes ? | seulement celles qui n'ont pas de corps | **toutes** |

---

## 4 quater. Java vs Python : d'où vient la confusion (et qui est vraiment implicite)

Ton intuition était juste **à moitié**, et c'est ce mélange qui perd :

| Question | Java | Python |
|---|---|---|
| Le parent **existe-t-il** dans l'objet ? | Toujours (le type est fixé, les champs alloués) | Toujours (`isinstance` vrai, méthodes héritées accessibles) |
| Le champ `id` **existe-t-il** ? | **Toujours**, avec sa valeur par défaut (`null`) | **Seulement si on lui a assigné** `self.id = ...` |
| L'initialiseur du parent est-il appelé ? | **Oui, automatiquement**, même si tu ne fais rien | **Non** : il faut écrire `super().__init__()` |

### a) Les trois cas Python, mesurés

```python
class Entity:
    kind = "entity"                 # attribut de CLASSE
    def __init__(self):
        self.id = 1                 # attribut d'INSTANCE, créé à l'EXÉCUTION
        print("Entity.__init__")

class OrderNoSuper(Entity):
    def __init__(self):
        print("OrderNoSuper.__init__   (pas de super)")

class OrderInheritsInit(Entity):
    pass                            # aucune __init__ : elle est reprise de Entity

class OrderWithSuper(Entity):
    def __init__(self):
        super().__init__()
        print("OrderWithSuper.__init__")
```

Sortie réelle :

```
--- 1) enfant qui définit __init__ SANS super()
OrderNoSuper.__init__   (pas de super)
isinstance(o, Entity) : True
o.kind (attribut de classe) : entity
hasattr(o, 'id') : False
o.id -> AttributeError : 'OrderNoSuper' object has no attribute 'id'

--- 2) enfant qui ne définit AUCUNE __init__
Entity.__init__
hasattr(o2, 'id') : True -> 1

--- 3) enfant avec super().__init__()
Entity.__init__
OrderWithSuper.__init__
hasattr(o3, 'id') : True -> 1
```

Trois leçons importantes :

1. **Cas 1** — l'enfant *est* un `Entity` (`isinstance` → `True`), il hérite des
   attributs de **classe** (`kind`), mais `self.id` **n'existe pas** : personne ne
   l'a créé. En Java, `id` existerait avec la valeur `null` — le stockage est
   prévu par la déclaration, même si personne ne l'a rempli.
2. **Cas 2** — si l'enfant **ne définit pas** `__init__`, alors celle du parent
   est **héritée et appelée** normalement. Python n'est donc pas « anti-super » :
   il est *explicite* **seulement quand tu redéfinis** `__init__`.
3. **Cas 3** — l'appel explicite remonte la chaîne, et l'ordre est l'inverse de
   l'intuition : parent **d'abord**, enfant **ensuite** (identique à Java).

### b) Pourquoi cette différence de philosophie

- **Java = langage à déclarations.** Le compilateur connaît la structure de
  l'objet avant l'exécution : il sait qu'un `Order` contient un `id`, il sait
  qu'un constructeur doit remonter à `Object`. Il peut donc **insérer** `super()`
  sans risque.
- **Python = langage à exécution.** Un objet est (schématiquement) un dictionnaire
  d'attributs plus un lien vers sa classe. `self.id = 1` **crée** l'attribut à ce
  moment-là ; rien ne le prévoit à l'avance. Le langage ne peut donc pas
  « appeler le parent pour toi » de façon fiable : `__init__` est une **méthode
  ordinaire**, simplement appelée par `C()` après `__new__`.

Conséquence pratique, le vrai piège des deux côtés :

| Oubli | Java | Python |
|---|---|---|
| Oublier d'appeler le parent | **impossible** (compilateur) | possible → `AttributeError` **plus tard**, ailleurs |
| Oublier de déclarer un champ | impossible | normal (pas de déclaration) |
| Erreur typique | « pourquoi `super()` est-il implicite ? » | « pourquoi `self.id` n'existe pas ? » |

En Python, le bug est donc **silencieux au départ** : `o = OrderNoSuper()` ne
plante pas. Il explose seulement au premier accès à `o.id`, souvent loin de la
cause. C'est ce qui rend le `super().__init__()` explicite **obligatoire en
pratique** dès qu'on redéfinit `__init__`.

### c) Le même TP 4, écrit dans les deux langages

```java
// Java : rien à écrire, tout est implicite
public class Order extends Entity {
  private double totalValue;
  public Order() { }            // super() inséré par le compilateur
}
```

```python
# Python : tout ce qui touche au parent doit être écrit
class Order(Entity):
    def __init__(self, total_value):
        super().__init__()          # ← obligatoire si Entity.__init__ fait un travail
        self.total_value = total_value
```

Et l'équivalent de nos interfaces, qui n'a **rien** à voir avec `super()` :

```python
# Java : interface + implements, méthodes obligatoires
# Python : héritage multiple + ABC, pas de constructeur dans l'interface
from abc import ABC, abstractmethod

class PersistenceService(ABC):          # équivalent d'une interface
    @abstractmethod
    def save(self, entity): ...
    @abstractmethod
    def find_by_id(self, id): ...

class NameSearchablePersistenceService(PersistenceService, ABC):
    @abstractmethod
    def find_by_name(self, name): ...

class OrderPersistenceService(PersistenceService):   # ISP respecté : pas de find_by_name
    def save(self, entity): ...
    def find_by_id(self, id): ...
```

Différence notable : en Python, `ABC` **empêche** l'instanciation si une méthode
abstraite manque (comme Java), mais uniquement parce que `ABCMeta` le vérifie
**à l'exécution**. En Java, c'est le compilateur qui refuse le fichier.

### d) La phrase à retenir

> **Java est implicite sur l'initialisation, explicite sur les déclarations.
> Python est explicite sur l'initialisation, implicite sur les déclarations.**

D'où le choc quand on passe de l'un à l'autre : les deux langages ont choisi
d'être « gentils » sur des choses opposées.

---

## 5. Le troisième `extends` : la généricité

```java
class Entity { }
class Order extends Entity { }

List<Order> orders = new ArrayList<>();
List<Entity> entities = orders;   // ❌ ne compile pas : List<Order> n'est pas List<Entity>
```

`extends` dans les chevrons n'a **rien à voir** avec l'héritage de code :

- `<T extends Entity>` sur une **déclaration** = borne : T doit être `Entity`
  ou une sous-classe. Sans borne, `T` ne serait qu'un `Object` et on ne pourrait
  pas appeler `entity.getId()`.
- `? extends Entity` sur un **usage** = « n'importe quel type qui est un
  `Entity` ». Utile pour **lire** (`Number n = list.get(0);`), interdit pour
  **écrire** (on ne sait pas de quel sous-type exact il s'agit).

`? super Order`, son miroir, sert à **écrire** : `list.add(order)`.

---

## 6. Récapitulatif « que choisir ? »

| Situation | Réponse |
|---|---|
| « une `Voiture` **est un** `Vehicule` » | `class Voiture extends Vehicule` |
| « cette classe **sait faire** `save` et `delete` » | `class X implements PersistenceService<T>` |
| « ce contrat **contient** aussi un autre contrat » | `interface A extends B` |
| « je veux juste **réutiliser** du code » (pas de relation « est un ») | **composition** : `private final Autre autre;` |
| « ma mère n'a pas de constructeur vide » | `super(args);` en première ligne, obligatoire |

## 7. Erreurs classiques

| Erreur | Cause | Correction |
|---|---|---|
| `constructor Mere in class Mere cannot be applied to given types` | mère sans constructeur vide | écrire `super(...)` |
| `super() must be the first statement` | code avant `super()` | déplacer l'appel en tête |
| `cannot inherit from final class` | mère `final` | supprimer `final` ou utiliser la composition |
| `X is not abstract and does not override abstract method findByName` | classe qui implémente une interface sans écrire toutes les méthodes | implémenter, ou choisir une interface plus petite (ISP !) |
| « je croyais que `implements Order` créait une `Order` » | confusion **type paramètre** / **héritage** | `<Order>` = « je stocke des Order », rien de plus |

## 8. Mini-quiz

1. Dans `class OrderPersistenceService implements PersistenceService<Order>`,
   quelle est la classe mère ? Faut-il écrire `super()` ?
2. `interface B extends A` : `B` récupère-t-elle les **champs** de `A` ?
3. Pourquoi ce code ne compile-t-il pas ?
   ```java
   class Mere { Mere(String s) {} }
   class Fille extends Mere { Fille() {} }
   ```
4. Peut-on écrire une instruction avant `super()` ?

<details>
<summary>Réponses</summary>

1. `Object` (implicite). Non : son constructeur vide est appelé automatiquement
   et il n'y a aucun état à initialiser.
2. Non : une interface n'a que des **constantes** (`public static final`) et des
   méthodes. `extends` y fusionne des **contrats**, pas du code.
3. L'appel implicite `super()` cherche un constructeur `Mere()` sans argument,
   qui n'existe pas. Il faut écrire `super("...")` dans `Fille()`.
4. Non. `super(...)` (ou `this(...)`) doit être la première instruction d'un
   constructeur.
</details>
