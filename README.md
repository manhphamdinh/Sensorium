# CHANGES FROM HUGE CHANGES
## 1. Gameplay loop changed from [Session -> Completion] to [Session + Progress -> Completion]:
- Session -> Completion:
  - Session: Every time Player opens a puzzle, they play from scratch.
  - Completion: All solved boxes and puzzles are saved permanently and does not affect Session.
- Session + Progress -> Completion:
  - Session + Progress:
    - Session: Checks for already solved boxes and puzzles, PREVENTING REPEATED function calls.
    - Progress: Each puzzle has its corresponding Progress, instead of replaying from scratch, the puzzle's progress gets saved and loaded every time the Player opens it. Once the puzzle is complete, its progress gets reset, preserving the puzzle's replayability.
  - Completion: All solved puzzles and boxes are saved permanently and does not affect Progress or Session.
## 2. Puzzle Fragment changes
- animation() (from PuzzleBaseFragment):
  - Renamed to updatePuzzle.
  - Old logic is now split into Data update, Audio (new) and UI.
  - No longer relies on getIdentifier() (slow). Receives box (ImageView) and its index (int) directly.
- All Puzzle Fragments have been updated to accommodate changes to Gameplay loop and Base Fragment while retaining their original logic.
- Puzzle 1 and puzzle 29 logic has been rewritten due to unfathomably abysmal dogshit code.
- Debug mode is added to Puzzle 7 for testing. 
