# SM-2026
## Aplikacja do śledzenia nawyków
Aplikacja ma za zadanie pomagać użytkownikowi w budowaniu pozytywnych nawyków i ograniczaniu tych negatywnych. Ma umożliwiać codzienne zaznaczanie wykonanych nawyków, a następnie analizować ich liczbę oraz prezentować podsumowanie dnia i statystyki w dłuższym okresie.
Dodatkowo użytkownik będzie mógł wybrać bardziej wyspecjalizowane, wbudowane nawyki, np. związane ze stopniową poprawą godziny wstawania albo z ograniczaniem zbyt długiego korzystania z wybranych stron i aplikacji.

# Roadmap

| Priorytet | Funkcja |
|-----------|---------|
| **Must** | Zarządzanie nawykami (dodawanie, edycja, usuwanie) |
| **Must** | Podstawowe ustawienia nawyku: nazwa, typ (pozytywny lub negatywny) i częstotliwość (np. codziennie, 3 razy w tygodniu) |
| **Must** | Codzienne oznaczanie wykonania nawyku |
| **Must** | Kalendarz z historią wykonanych nawyków |
| **Must** | Podsumowanie dnia |
| **Must** | Statystyki tygodniowe i miesięczne: liczba wykonań, skuteczność w procentach, aktualna i najdłuższa seria |
| **Must** | Wbudowany nawyk: stopniowa poprawa godziny wstawania |
| **Must** | Wbudowany nawyk: ograniczenie czasu na wybranych stronach i w aplikacjach |
| **Should** | Ukrywanie zakończonych nawyków z zachowaniem ich historii i statystyk |
| **Should** | Dedykowane statystyki dla każdego wbudowanego nawyku, dopasowane do sposobu jego mierzenia |
| **Should** | Rozszerzona biblioteka wbudowanych nawyków |
| **Should** | Nawyki mierzalne (np. liczba szklanek wody, minuty) |
| **Should** | Przypomnienia |
| **Could** | Notatki do wybranych dni |
| **Could** | Widget na ekran główny |
| **Could** | Import danych |

- **Must**: podstawowe działanie aplikacji. Bez tych funkcji aplikacja nie spełnia swojego zadania.
- **Should**: ważne dodatki, które znacznie poprawiają wygodę użytkowania, ale aplikacja działa także bez nich.
- **Could**: opcjonalne rzeczy, które mogą się pojawić, ale nie muszą.

## Technologie
- Kotlin + Jetpack Compose
- Android Studio

## Grupa
- Maciej Michoń

## Postępy prac

- **12–15.10.2026**: ustalenie zakresu aplikacji i przygotowanie roadmapy funkcji (Must / Should / Could)
- **17.10.2026**: przygotowanie środowiska: utworzenie projektu w Android Studio (Kotlin + Jetpack Compose), konfiguracja Gradle
