# Roman Urdu PDF Reader 📖✨

A native Android application built with **Kotlin** and **Jetpack Compose** that allows you to read English PDF books and instantly see Roman Urdu meanings in a popup anchored immediately above any tapped English word — without leaving the book or losing your reading position.

---

## 🌟 Core Features

1. **Instant Word Tapping in PDF**: Tap any English word inside a PDF page to see its Roman Urdu meaning in a popup directly above the word.
2. **Context-Aware Disambiguation**: Determines the meaning based on the surrounding sentence (e.g., *“book a hotel”* → *“Pehle se seat ya kamra mehfooz karna”* vs *“read a book”* → *“Kitab / Pustak”*; *“river bank”* vs *“money bank”*; *“light bag”* vs *“room light”*).
3. **Rich Offline Roman Urdu Dictionary**: Over 770+ curated foundational English words with accurate Roman Urdu translations, parts of speech, and keyword contexts stored locally on device.
4. **Smart Lemmatizer**: Automatically detects inflected word forms (plurals `books` → `book`, past tense `cried` → `cry`, continuous `running` → `run`, adverbs `happily` → `happy`, irregulars `went` → `go`).
5. **Optional Free Online Lookup**: When an uncommon word is not in the offline dictionary, an optional one-tap online search queries public dictionary endpoints and caches the result for future offline reading.
6. **Text-to-Speech (TTS) Pronunciation**: One-tap native audio pronunciation for any tapped or saved word.
7. **Storage Access Framework (SAF)**: Securely import multiple PDF books from your device's internal storage or SD card.
8. **Reading Progress & Bookmarks**: Automatically saves your current page, scroll percentage, and bookmarks in a local Room database.
9. **Personal Vocabulary Notebook**: Bookmark words with their Roman Urdu meanings, sentence context, source book, and page number to review later.
10. **Reading Themes**: Switch effortlessly between **Day (Light)**, **Night (Dark/OLED)**, and **Paper (Sepia)** reading modes, plus adjust zoom scale.
11. **Scanned PDF / OCR Detection**: Automatically detects when a PDF contains scanned images without a text layer and alerts the user with helpful OCR guidance.

---

## 🏗️ Architecture & Tech Stack

- **Language**: Kotlin 2.0 (100% Native)
- **UI Framework**: Jetpack Compose with Material 3
- **Local Database**: Room 2.6.1 (SQLite) with Coroutines and StateFlow
- **PDF Viewer & Text Layer**: Embedded Mozilla PDF.js rendering engine in WebView with bidirectional JavaScript bridge (`AndroidBridge`), delivering exact CSS bounding rects for sub-millisecond popup placement
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with Clean Repository Pattern
- **JSON Serialization**: Google Gson
- **Voice / Audio**: Android Native TextToSpeech (`android.speech.tts.TextToSpeech`)

---

## 📁 Project Structure

```
├── app
│   ├── build.gradle.kts                   # App module build configuration (Compose, Room, WebKit)
│   ├── proguard-rules.pro                 # Proguard optimization rules
│   └── src
│       ├── main
│       │   ├── AndroidManifest.xml        # App declaration, permissions, SAF filters
│       │   ├── assets
│       │   │   ├── dictionary
│       │   │   │   └── roman_urdu_dictionary.json  # Comprehensive offline dictionary
│       │   │   └── pdfjs
│       │   │       ├── pdf.min.js         # PDF.js library
│       │   │       ├── pdf.worker.min.js  # Worker thread
│       │   │       ├── viewer.css         # Responsive mobile styling & themes
│       │   │       ├── viewer.html        # HTML5 text-layer container
│       │   │       └── viewer.js          # Word tap detector & bridge
│       │   ├── java/com/romanurdu/pdfreader
│       │   │   ├── data
│       │   │   │   ├── dao            # Room DAOs (BookDao, BookmarkDao, VocabularyDao, CacheDao)
│       │   │   │   ├── database       # AppDatabase singleton
│       │   │   │   ├── model          # Entities (BookEntity, VocabularyEntity, DictionaryEntry)
│       │   │   │   └── repository     # BookRepository, DictionaryRepository
│       │   │   ├── dictionary
│       │   │   │   ├── ContextDisambiguator.kt  # Sentence syntax & keyword disambiguation
│       │   │   │   ├── DictionaryEngine.kt     # In-memory dictionary & cache coordinator
│       │   │   │   ├── Lemmatizer.kt           # Inflection & root form reducer
│       │   │   │   └── OnlineLookupClient.kt   # Free public API fallback
│       │   │   ├── tts
│       │   │   │   └── TtsManager.kt           # Text-to-speech pronunciation
│       │   │   ├── ui
│       │   │   │   ├── components     # WordPopupCard, PdfViewerWebView, BookCard, etc.
│       │   │   │   ├── navigation     # NavRoutes, NavItem, NavigationBar
│       │   │   │   ├── screens        # LibraryScreen, ReaderScreen, VocabularyScreen, DictionarySearchScreen
│       │   │   │   ├── theme          # Theme, Color, Type, ReadingTheme
│       │   │   │   └── viewmodel      # BookViewModel, ReaderViewModel, VocabularyViewModel
│       │   │   ├── utils
│       │   │   │   ├── FileUtils.kt            # SAF file copier & formatting
│       │   │   │   └── PdfBridgeInterface.kt   # JavascriptInterface bridge
│       │   │   ├── MainActivity.kt        # Compose container & Navigation Host
│       │   │   └── RomanUrduReaderApp.kt  # Application entry point & dictionary pre-warmer
│       │   └── res                        # Adaptive icons, styles, strings, XML rules
│       └── test/java/com/romanurdu/pdfreader
│           ├── ContextDisambiguatorTest.kt # Sentence context disambiguation tests
│           ├── DictionaryEngineTest.kt     # Dictionary integrity tests
│           ├── LemmatizerTest.kt           # Plurals, past-tense, gerunds tests
│           └── PopupAnchoringTest.kt       # Bounding box & popup positioning tests
├── sample_books
│   ├── Sample_English_Story.pdf           # Searchable PDF book for immediate testing
│   └── Sample_Scanned_Document.pdf        # Scanned PDF without text for OCR warning test
├── scripts
│   ├── verify_reader_logic.py             # Automated standalone test suite runner
│   ├── generate_sample_pdf.py             # Script generating sample test book
│   └── generate_scanned_pdf.py            # Script generating scanned test book
├── build.gradle.kts                       # Root project build file
├── settings.gradle.kts                    # Root project settings
├── gradlew / gradlew.bat                  # Gradle Wrapper scripts
└── .gitignore                             # Clean Git ignore rules
```

---

## 🚀 Building & Installing the APK

### Prerequisites
1. **JDK 17 or higher** installed (`java -version`).
2. **Android SDK** (API 34 or 35) with `ANDROID_HOME` configured.
3. Android device or emulator with Android 7.0+ (API 24+).

### Step 1: Build the Debug APK
Open a terminal in the project directory and run:

```bash
# On Linux / macOS:
./gradlew assembleDebug

# On Windows:
gradlew.bat assembleDebug
```

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: Install on Your Android Phone
Connect your phone with USB Debugging enabled, then run:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

*(Alternatively, transfer `app-debug.apk` directly to your phone via USB or WhatsApp/Telegram/Drive and tap to install).*

---

## 🧪 Running Automated Tests

Run the unit test suite with Gradle:

```bash
./gradlew test
```

Or run the bundled standalone verification test suite:

```bash
python3 scripts/verify_reader_logic.py
```

All tests verify:
- Lemmatizer rules (`running` → `run`, `books` → `book`, `happily` → `happy`, `went` → `go`).
- Sentence context disambiguation for multi-meaning words (`book`, `light`, `bank`).
- Boundary and clamping math for popup placement above/below tapped words.

---

## 📖 How to Test Word Tapping & Features

1. Launch **Roman Urdu PDF Reader** on your phone.
2. Tap **"+ Import PDF"** on the Library screen.
3. Select any PDF (or use `sample_books/Sample_English_Story.pdf`).
4. Tap any English word on the page (e.g., *"courage"*, *"illuminated"*, *"bank"*, *"book"*).
5. Notice:
   - The small popup appears **immediately above** the word with its Roman Urdu meaning.
   - The surrounding sentence context is used to pick the correct definition.
   - Tap the speaker icon to hear the English pronunciation.
   - Tap the bookmark ribbon to save the word to your Vocabulary list.
   - Tap anywhere outside or tap another word instantly without losing your page or scroll position!
6. Test OCR Detection:
   - Import `sample_books/Sample_Scanned_Document.pdf`.
   - The app immediately shows the polite **"Scanned PDF Detected"** banner explaining that digital text or OCR is needed for word tapping.

---

## 📤 Uploading to Your GitHub Repository

The project is structured and ready for GitHub:

```bash
# Initialize git (if not already done)
git init

# Add all files
git add .

# Make initial commit
git commit -m "Initial commit: Complete Roman Urdu PDF Reader native Android app"

# Add your GitHub repository remote
git remote add origin https://github.com/<your-username>/roman-urdu-pdf-reader.git

# Push to main branch
git branch -M main
git push -u origin main
```

---

## 🔒 Privacy & Offline Reliability

- **100% On-Device by Default**: Imported books, reading progress, and saved vocabulary stay in your phone's private internal storage.
- **No Paid API Keys**: No secrets or API credentials required.
- **Works Completely Offline**: The built-in 770+ word Roman Urdu dictionary handles regular reading without needing Wi-Fi or mobile data.
