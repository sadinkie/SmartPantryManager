# Smart Pantry Manager

A Java Android app that helps cut food waste by suggesting recipes you can cook
using **only the ingredients you already have**. No shopping trip required.

- **Module:** Mobile App Development 700, Richfield Graduate Institute of Technology
- **Author:** Nomandla Ndlovu (Student No. 402305988)
- **Language / IDE:** Java, Android Studio
- **Minimum SDK:** API [24] (Android [7.0])

## Features

- **Pantry management:** add, edit and delete ingredients (name, quantity, unit, optional expiry date)
- **Expiry highlighting:** expired items show red; items expiring within 3 days show orange (switchable in Settings)
- **20 pre-loaded recipes**, seeded into the database on first run
- **Strict-matching suggestions:** a recipe is suggested only if *every* ingredient is in the pantry in at least the required quantity
- **Robust matching:** ignores letter case and extra spaces, handles singular/plural names (`tomato` / `Tomatoes`), converts units (g/kg, ml/l/tsp/tbsp/cup), and adds up duplicate pantry entries
- **Recipe detail:** full ingredient list with a tick or cross per ingredient, plus numbered method steps
- **Helpful empty states** when the pantry is empty or nothing matches
- **Optional bonus:** an "Almost There" section (recipes missing exactly one ingredient), off by default and clearly separated from the strict list
- **Input validation** on the ingredient form
- **Settings** saved with SharedPreferences

## Screens

| Screen | Purpose |
|---|---|
| Pantry (tab) | Lists all ingredients; swipe to delete (with Undo); + button to add |
| Add / Edit Ingredient | Form with validation, unit dropdown and date picker |
| Suggestions (tab) | Recipes the user can cook right now |
| Recipe Detail | Ingredients (have / missing) and method |
| Settings (tab) | "Almost There" switch, expiring-soon alerts, About |

## Database choice: SQLite

I chose **SQLite** through `SQLiteOpenHelper` because:

- The app works **fully offline**, so there is no server, account or API key to set up.
- The data is **relational** (a recipe has many ingredient lines), which fits tables with a foreign key.
- It **persists** between sessions: data is stored in a real file on the device.
- It matches the approach taught in the module, and anyone can run the project without any extra configuration.

**Tables:** `ingredients` (pantry), `recipes`, and `recipe_ingredients`
(linked to `recipes` by `recipe_id`, with `ON DELETE CASCADE`).

## How the strict-matching rule works

For every recipe, and for each ingredient it needs:

1. The ingredient name is **normalized** (lowercase, trimmed, descriptors like "fresh" removed, plural made singular).
2. Quantities are **converted to a base unit** (grams, millilitres or pieces).
3. The pantry total for that ingredient is looked up (duplicate entries are added together).
4. If it is missing or the quantity is too small, the recipe is **excluded**.

A recipe appears only if nothing is missing. Mass and volume are never converted
into each other, so `300 g chicken` is not satisfied by `2 pieces of chicken`.

## Project structure

```
app/src/main/java/com/example/smartpantrymanager/
├── MainActivity.java            (toolbar + bottom navigation host)
├── SmartPantryApp.java          (seeds recipes on first launch)
├── adapter/    PantryAdapter, RecipeAdapter
├── database/   DatabaseHelper, RecipeSeeder
├── logic/      NameNormalizer, UnitConverter, RecipeMatcher
├── model/      Ingredient, Recipe, RecipeIngredient
├── ui/         PantryFragment, SuggestionsFragment, SettingsFragment,
│               AddEditIngredientActivity, RecipeDetailActivity
└── util/       AppPreferences
```

## Setup and run

1. Install a recent version of **Android Studio**.
2. Clone the repository:
```
   git clone https://github.com/sadinkie/SmartPantryManager.git
```
3. In Android Studio choose **File > Open** and select the cloned folder.
4. Wait for the Gradle sync to finish.
5. Create or start an emulator (Device Manager) or connect a phone with USB debugging.
6. Press **Run**. The first launch seeds the 20 recipes automatically.

**Reset the app's data:** long-press the app icon > App info > Storage > Clear storage.

## Tests

Unit tests for the matching logic are in `app/src/test/java/.../logic/`.
Run them in Android Studio (right-click the `logic` test folder > Run) or from a terminal:

```
./gradlew test        (macOS / Linux)
gradlew.bat test      (Windows)
```

## Out of scope

As required by the brief, the app uses **no Google Maps, mapping SDKs or location services**, and has no payments.