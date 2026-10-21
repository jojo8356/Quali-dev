# Java vu depuis Python — la carte mentale qui manque

> Tu ne trouves pas Java « illogique » : tu le trouves **explicite là où Python
> est implicite, et implicite là où Python est explicite**. C'est exactement ça,
> et une fois la carte posée, ça devient mécanique.

---

## 0. La différence de philosophie en 3 lignes

| | Python | Java |
|---|---|---|
| Devise | **« si ça marche à l'exécution, c'est bon »** | **« si ça compile, c'est bon »** |
| Typage | **structurel** (duck typing) : seule la *forme* compte | **nominal** : seul le *nom du type* compte |
| Vérification | à l'exécution (`AttributeError`, `TypeError`) | à la compilation (`javac` refuse le fichier) |

Conséquence directe : **une interface Java, c'est ce que Python fait déjà
sans le dire.** Ton `def save(self, entity)` qui prend n'importe quel objet
« qui a un `.id` » est *déjà* une interface — sauf que personne ne l'a écrite,
et que rien ne la vérifie.

```python
# Python : ça passe, puis ça casse en production
def save(service, order):
    service.save(order)          # aucun contrôle : et si service n'a pas de .save ?

# Java : ça ne compile pas, donc ça ne part jamais en prod
void save(PersistenceService<Order> service, Order order) {
    service.save(order);         // vérifié à la compilation
}
```

C'est **le** point à comprendre avant tout le reste : chaque `extends`,
`implements`, `<T>` que tu trouves lourd est **le prix** pour que quelqu'un
d'autre (le compilateur) fasse le travail de vérification à ta place.

---

## 1. Le dictionnaire Python → Java

| Python | Java | La vraie différence |
|---|---|---|
| `class Order:` | `public class Order` | `public` + fichier `Order.java` + nom identique |
| `class Order(Entity):` | `class Order extends Entity` | même idée ; **une seule** mère |
| `class C(A, B):` | `class C implements A, B` | Java : multiple **de contrats**, pas de code |
| `class PersistenceService(ABC)` | `interface PersistenceService` | en Java, pas d'état du tout dans l'interface |
| `@abstractmethod` | `void save(T e);` (sans `{}`) | l'absence de corps **est** le mot-clé `abstract` |
| méthode de ABC avec un corps | `default void x() { }` (Java 8+) | identique dans l'idée |
| `typing.Protocol` | `interface` | Protocol = structurel, interface = nominal |
| `def __init__(self, a):` | `public Order(int a) { }` | **même nom que la classe**, pas de `self` |
| `self.x = a` | `this.x = a;` **et** `private int x;` | Java exige de **déclarer** le champ |
| `super().__init__()` | `super();` | **obligatoire en Python, implicite en Java** |
| `self` | `this` | `this` existe partout, se déclare nulle part |
| `__str__` | `toString()` | + le décorateur `@Override` |
| `@property` | `getX()` / `setX()` | pas de propriétés « magiques » en Java |
| `@dataclass` | `record Point(int x, int y) {}` (Java 16+) | le plus proche équivalent moderne |
| `isinstance(o, C)` | `o instanceof C` | Java : aussi un **pattern** `if (o instanceof User u)` |
| `None` | `null` | `null` est un piège : voir §4 |
| `int` (infini) | `int` (32 bits) / `long` (64) / `Long` (objet) | `Long` peut être `null`, `long` non |
| `list[T]` | `List<T>` + `new ArrayList<>()` | types génériques **effacés** à l'exécution |
| `import x.y` | `package x.y;` + `import x.y.Z;` | le **dossier** doit correspondre au package |
| `pip install` | Maven/Gradle (`pom.xml`) | dépendances déclarées, pas de `requirements` |
| `pytest` | JUnit (`@Test`) | mêmes idées |

---

## 2. « extends partout » : d'où ça vient

En Java, **tout** ce qui hérite le dit, et il y a **quatre** `extends` différents
(voir `extends-explique.md`). Le réflexe à avoir en lisant :

```
class   OrderPersistenceService implements PersistenceService<Order>
│       │                              │          │           │
│       │                              │          │           └── je stocke des Order
│       │                              │          └── contrat « CRUD de base »
│       │                              └── mot-clé : promesse, pas héritage de code
│       └── nom de ma classe
└── mot-clé
```

| Je vois… | Je pense… |
|---|---|
| `class A extends B` | héritage **de code** (champs + méthodes) — comme Python |
| `interface A extends B` | j'ajoute les méthodes de `B` à mon contrat — **pas de code, pas de constructeur** |
| `class A implements B` | je m'engage à écrire **toutes** les méthodes de `B` |
| `<T extends Entity>` | contrainte de type : « T doit être un Entity » |
| `? extends Entity` | « un type qui est un Entity » (lecture seule) |

En Python, les 3 premiers cas sont **le même mot** (`(Entity)`), et le 4ᵉ
n'existe pas (pas de génériques vérifiés). D'où la sensation de « partout ».

---

## 3. L'implicite de Java (ce que le compilateur écrit à ta place)

| Ce que tu écris | Ce que le compilateur ajoute |
|---|---|
| `class Order { }` | `extends Object` |
| un constructeur sans `super()` | `super();` en première ligne |
| un champ `private int x;` | l'initialisation `x = 0` (jamais « pas de valeur ») |
| `private Long id;` | `id = null` (référence) |
| une méthode sans `private/public` | `package-private` (visible dans le package) |
| les méthodes d'une `interface` | `public abstract` par défaut |
| les champs d'une `interface` | `public static final` (constantes) |
| `Order o = new Order();` | allocation + constructeur + retour d'une **référence** |
| `list.add("a")` sur `List<String>` | vérification du type **au compilateur** (effacé à l'exécution) |
| `1L` passé à `setId(Long)` | `Long.valueOf(1L)` (autoboxing) |

**À l'inverse, Java est explicite là où Python est magique** : il faut
**déclarer** chaque champ, **nommer** chaque type, **écrire** chaque promesse.
Ton inconfort vient de là : tu as l'habitude d'un langage où l'objet *devient* ce
qu'on en fait, alors qu'ici il *est déclaré* une fois pour toutes.

---

## 4. Les 5 pièges qui coûtent le plus cher quand on vient de Python

1. **`null` n'est pas `None`.** En Java, `null` existe pour les objets, pas pour
   `int`/`boolean`, et il n'y a pas d'「Optional」 implicite. Un `Long id` non
   initialisé vaut `null` → `NullPointerException` au premier déréférencement.
   Python : même problème avec `None`, mais les messages sont plus clairs.
2. **`==` n'est pas `=` de valeur pour les objets.** `str1 == str2` compare les
   **références** pour tout ce qui n'est pas primitif → utiliser `.equals()`
   (sauf `enum` et constantes). C'est le `is` de Python… mais **par défaut**.
3. **Les tableaux et génériques sont effacés.** `List<Order>` à l'exécution est
   juste une `List` : pas d'`isinstance` possible sur `T`. En Python, tout est
   disponible à l'exécution.
4. **Une seule classe mère.** Pas de `class C(A, B)`. Le multiple passe par les
   interfaces (ou la composition).
5. **Tout est typé, donc tout est « bavard ».** Un fichier fait souvent 2-3 fois
   la taille de son équivalent Python. Ce n'est pas du bruit : c'est le contrat
   que le compilateur vérifie.

---

## 5. Les interfaces existent en Python (tu les écrivais sans le savoir)

Ton code Python fait déjà de l'ISP sans le nommer :

```python
class UserPersistenceService:
    def save(self, entity): ...
    def find_by_name(self, name): ...

class OrderPersistenceService:
    def save(self, entity): ...          # pas de find_by_name : ISP respecté

# n'importe quelle fonction qui appelle .save() accepte les deux :
def persister(service, entity):
    service.save(entity)                 # duck typing : « forme » suffisante
```

Personne n'a écrit le contrat, mais **il existe dans ta tête**. Java le rend
**explicite et vérifié** :

```java
interface PersistenceService<T extends Entity> {
    void save(T entity);
    // …
}
void persister(PersistenceService<Order> service, Order entity) {
    service.save(entity);   // garanti par le compilateur
}
```

Deux façons de le faire en Python, si tu veux la même sécurité :

```python
from typing import Protocol

class PersistenceService(Protocol):
    def save(self, entity): ...
    def find_by_id(self, id): ...

def persister(service: PersistenceService, entity): ...  # mypy vérifie !
```

**Conclusion** : Java ne t'impose rien de nouveau conceptuellement. Il te
demande d'**écrire** ce que Python te laisse **supposer**. C'est plus verbeux,
c'est plus sûr, et c'est tout.

---

## 6. Comment lire une ligne Java sans paniquer (méthode en 4 temps)

1. **Couper aux espaces** : `public class Order extends Entity implements X`
   → `public` | `class Order` | `extends Entity` | `implements X`.
2. **Classer le mot-clé** avec le tableau du §2 : *code hérité* / *contrat* /
   *contrainte de type*.
3. **Repérer ce qui est écrit vs. implicite** avec le tableau du §3.
4. **Traduire en Python**, puis vérifier que le sens survit :
   `class Order(Entity): ...` → OK, identique.

Entraînement sur une ligne de nos TP :

```java
public class UserPersistenceService implements NameSearchablePersistenceService<User>
```
→ « une classe publique `UserPersistenceService`, qui **promet** de réaliser le
contrat `NameSearchablePersistenceService` appliqué à `User` ; donc elle doit
écrire `save`, `delete`, `findById` **et** `findByName` ».
Python équivalent : `class UserPersistenceService(NameSearchablePersistenceService)`.

---

## 7. Plan de survie (une semaine, 20 min/jour)

| Jour | Objectif | Exercice sur le repo |
|---|---|---|
| 1 | Lire une classe Java à voix haute | ouvrir `Entity.java`, `Order.java`, dire chaque mot-clé |
| 2 | `extends` vs `implements` | refaire le TP4 sans regarder (2 interfaces, 2 classes) |
| 3 | Constructeurs & `super()` implicite | écrire une classe mère avec constructeur à arguments, la faire planter puis corriger |
| 4 | Interfaces + génériques | ajouter une entité `Invoice` et choisir la bonne interface |
| 5 | `null`, `equals`, autoboxing | écrire 3 petits tests qui cassent exprès |
| 6 | Lire du code d'autrui | lire `UserPersistenceService` ligne par ligne, prédire la sortie |
| 7 | Écrire 30 lignes sans aide | un `OrderService` avec une seule responsabilité |

Astuce : **raconte le code en français** avant de l'exécuter. « Cette classe
*promet*… donc elle *doit* écrire… ». Si tu ne sais pas dire la phrase, tu n'as
pas encore lu le mot-clé.

---

## 8. Les 6 phrases à retenir

1. Python = structurel et à l'exécution ; Java = nominal et à la compilation.
2. Une interface Java = ce que Python fait déjà en duck typing, mais **écrit et vérifié**.
3. `extends` classe = **code hérité** ; `extends` interface = **contrat cumulé** ;
   `implements` = **je m'engage** ; `<T extends X>` = **contrainte de type**.
4. Java rend **implicite** l'initialisation (constructeur parent, valeurs par
   défaut) et **explicite** les déclarations (champs, types, promesses).
5. `null`, `==`/`.equals`, effacement des génériques, une seule mère : les 4
   pièges à surveiller en priorité.
6. Si tu sais dire la phrase en français (« cette classe promet… donc elle doit
   écrire… »), le code Java n'est plus obscur : il est **redondant**, et c'est
   voulu.
