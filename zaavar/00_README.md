# Санхүү бүртгэлийн апп — ЯВЦЫН ШАЛГАЛТ 2026

Энэ хавтсанд таны бичих бүх кодыг **алхам алхмаар**, тайлбартай нь өгсөн.
`app/src/main/java/...` доторх `.kt` файлууд **хоосон загвар** — дээр нь тухайн файлын
**ҮҮРЭГ** ба **БИЧИХ ЗҮЙЛС** тайлбар, `import`-ууд бэлэн байгаа. Та `// TODO`-ийн оронд кодоо бичнэ.

> Бүрэн кодыг эмулятор (Pixel 8a) дээр ажиллуулж, функциональ 10 шаардлага болон
> техникийн 5 шалгуурыг бүгдийг нь шалгасан.

---

## 1. Архитектур (давхаргууд)

```
┌──────────────────────── UI (Compose) ────────────────────────┐
│  HomeScreen        EntryScreen        SettingsScreen         │  ← зөвхөн ХАРУУЛНА
└──────┬──────────────────┬──────────────────┬─────────────────┘
       │ uiState (StateFlow)  ↑ товч дарах (функц дуудах)
┌──────▼──────────────────▼──────────────────▼─────────────────┐
│  HomeViewModel     EntryViewModel     SettingsViewModel      │  ← ЛОГИК, төлөв
└──────┬──────────────────┬──────────────────┬─────────────────┘
       │ Flow / suspend
┌──────▼──────────────────▼─────┐   ┌────────▼────────────────┐
│  TransactionRepository        │   │  SettingsRepository     │  ← өгөгдлийн эх үүсвэрийг нууна
└──────┬────────────────────────┘   └────────┬────────────────┘
┌──────▼────────────────────────┐   ┌────────▼────────────────┐
│  TransactionDao → Room (SQLite)│   │  DataStore (settings)   │  ← ХАДГАЛАХ газар
└───────────────────────────────┘   └─────────────────────────┘

  MainActivity.onStop → ReminderScheduler → WorkManager → ReminderWorker → Мэдэгдэл
```

**Өгөгдлийн урсгал (жишээ: бүртгэл нэмэх)**
1. EntryScreen дээр ХАДГАЛАХ дарна → `viewModel.save()`
2. EntryViewModel → `repository.insert(transaction)`
3. Repository → `dao.insert()` → Room SQLite-д бичнэ
4. Room `getAll()` Flow-оор шинэ жагсаалт **автоматаар** илгээнэ
5. HomeViewModel-ийн `uiState` шинэчлэгдэнэ → HomeScreen өөрөө дахин зурагдана

---

## 2. Файлын бүтэц

```
app/src/main/
├── AndroidManifest.xml                ✅ бэлэн (зөвшөөрөл, Application, Activity)
├── res/values/strings.xml             ✅ бэлэн (бүх монгол текст)
└── java/com/example/financetracker/
    ├── FinanceApplication.kt          ✏️  Алхам 2 — апп асахад container үүсгэх
    ├── MainActivity.kt                ✏️  Алхам 7 — цорын ганц Activity
    ├── data/                          ── ӨГӨГДЛИЙН ДАВХАРГА
    │   ├── Transaction.kt             ✏️  Алхам 1 — @Entity (хүснэгт)
    │   ├── TransactionDao.kt          ✏️  Алхам 1 — @Dao (SQL, CRUD)
    │   ├── FinanceDatabase.kt         ✏️  Алхам 1 — @Database (singleton)
    │   ├── TransactionRepository.kt   ✏️  Алхам 2 — Repository
    │   ├── SettingsRepository.kt      ✏️  Алхам 2 — DataStore
    │   └── AppContainer.kt            ✏️  Алхам 2 — Repository-уудыг үүсгэх
    ├── ui/                            ── ДЭЛГЭЦИЙН ДАВХАРГА
    │   ├── AppViewModelProvider.kt    ✏️  Алхам 3 — ViewModel factory
    │   ├── FinanceApp.kt              ✏️  Алхам 3 — үндэс + TopAppBar
    │   ├── navigation/FinanceNavGraph.kt ✏️ Алхам 3 — 3 дэлгэцийн навигаци
    │   ├── home/HomeViewModel.kt      ✏️  Алхам 4
    │   ├── home/HomeScreen.kt         ✏️  Алхам 4
    │   ├── entry/EntryViewModel.kt    ✏️  Алхам 5
    │   ├── entry/EntryScreen.kt       ✏️  Алхам 5
    │   ├── settings/SettingsViewModel.kt ✏️ Алхам 6
    │   ├── settings/SettingsScreen.kt ✏️  Алхам 6
    │   └── theme/Theme.kt             ✅ бэлэн (өнгө)
    └── worker/                        ── МЭДЭГДЭЛ
        ├── NotificationHelper.kt      ✏️  Алхам 7
        ├── ReminderWorker.kt          ✏️  Алхам 7
        └── ReminderScheduler.kt       ✏️  Алхам 7
```

✅ = бэлэн, хөндөх шаардлагагүй  ✏️ = та бичнэ

---

## 3. Бичих дараалал

| Алхам | Заавар | Юу хийнэ |
|---|---|---|
| 1 | [01_ugugdliin_san_Room.md](01_ugugdliin_san_Room.md) | Room: Entity, DAO, Database |
| 2 | [02_repository_datastore.md](02_repository_datastore.md) | Repository, DataStore, AppContainer, Application |
| 3 | [03_viewmodel_factory_navigation.md](03_viewmodel_factory_navigation.md) | ViewModel factory, навигаци |
| 4 | [04_undsen_delgets.md](04_undsen_delgets.md) | Үндсэн дэлгэц |
| 5 | [05_nemeh_zasah_delgets.md](05_nemeh_zasah_delgets.md) | Нэмэх / засах дэлгэц |
| 6 | [06_tohirgoo_delgets.md](06_tohirgoo_delgets.md) | Тохиргооны дэлгэц |
| 7 | [07_medegdel_MainActivity.md](07_medegdel_MainActivity.md) | WorkManager мэдэгдэл, MainActivity |

⚠️ Алхам 1 дууссаны дараа **Build → Make Project** хийж Room зөв эсэхийг шалга.
Алхам 2–6-ын үед бусад файл дутуу тул зарим нэр улаан байх нь хэвийн.
**Бүх 7 алхам дууссаны дараа** апп бүрэн ажиллана.

💡 Зөвлөгөө: кодыг шууд хуулахын оронд **гараараа бичиж**, коммент бүрийг уншаарай —
багш "энэ мөр юу хийдэг вэ?" гэж асуухад хариулж чадах болно.

---

## 4. Шаардлага → код хаана байна

### Функциональ (10 оноо)
| № | Шаардлага | Хаана |
|---|---|---|
| 1 | Нэг нэгээр харуулах, тохиргооноос хамааран шүүх | `HomeViewModel.transactions` (flatMapLatest), `HomeScreen.TransactionCard` |
| 2 | `**** ₮`, дарахад бодит дүн | `HomeScreen.AmountText`, `HomeViewModel.toggleAmountVisibility` |
| 3 | ӨМНӨХ / ДАРААХ | `HomeViewModel.showPrevious/showNext`, `HomeButtons` |
| 4 | Засах, устгах | `HomeButtons` (ЗАСАХ, УСТГАХ), `HomeViewModel.deleteCurrent` |
| 5 | НЭМЭХ, ЗАСАХ → бүртгэлийн дэлгэц | `FinanceNavGraph` (`EntryDestination.createRoute`) |
| 6 | Цэснээс тохиргоо нээх | `HomeScreen` TopAppBar дээрх ⚙ |
| 7 | Хоосон үед зөвхөн НЭМЭХ идэвхтэй | `HomeButtons` → `enabled = ...` |
| 8 | Удаан дарахад засах | `TransactionCard` → `combinedClickable(onLongClick)` |
| 9 | Устгахын өмнө диалог | `DeleteConfirmationDialog` |
| 10 | Landscape | `HomeScreen` → `isLandscape` бол `Row`; бусад дэлгэц `verticalScroll` |

### Техникийн (10 оноо)
| № | Шалгуур | Хаана |
|---|---|---|
| 1 | Room (3) | `data/Transaction.kt`, `TransactionDao.kt`, `FinanceDatabase.kt` |
| 2 | ViewModel, Repository, Flow (2) | `ui/*/…ViewModel.kt`, `data/TransactionRepository.kt`, DAO-гийн `Flow` |
| 3 | DataStore (2) | `data/SettingsRepository.kt`, `AppContainer.kt` |
| 4 | 24 цаг тутмын мэдэгдэл (2) | `worker/ReminderScheduler.kt`, `ReminderWorker.kt`, `MainActivity.onStop` |
| 5 | Мэдэгдэл дарахад үндсэн дэлгэц (1) | `NotificationHelper.kt` → `PendingIntent` |

---

## 5. Шалгалтын үед асуугдаж болох асуултууд

- **Яагаад Repository хэрэгтэй вэ?** — ViewModel өгөгдөл хаанаас ирж байгааг мэдэхгүй байх; эх үүсвэрийг солиход ViewModel өөрчлөгдөхгүй.
- **Flow ба suspend-ийн ялгаа?** — `suspend` нэг удаа ажиллаад дуусна (insert/update/delete); `Flow` DB өөрчлөгдөх бүрт шинэ утга дамжуулна (унших).
- **Room ба DataStore-ийн ялгаа?** — Room = олон мөртэй хүснэгт (бүртгэлүүд); DataStore = цөөн key-value тохиргоо.
- **Утсыг эргүүлэхэд яагаад мэдээлэл алдагдахгүй вэ?** — Activity дахин үүсдэг ч ViewModel амьд үлддэг; диалогийн төлөв `rememberSaveable`-д.
- **Мэдэгдэл яаж ажилладаг вэ?** — `onStop`-д WorkManager-т 24 цагийн PeriodicWork захиална, `onStart`-д цуцална. Worker мэдэгдэл үүсгэж, `PendingIntent` нь MainActivity-г нээнэ.
- **`@PrimaryKey(autoGenerate = true)` юу хийдэг вэ?** — id-г Room өөрөө 1, 2, 3… гэж өгнө.
