package com.example.blackbox;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class PuzzleActivity extends AppCompatActivity {

    public static final String EXTRA_PUZZLE_ID = "puzzle_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        int puzzleId = getIntent().getIntExtra(EXTRA_PUZZLE_ID, -1);

        if (savedInstanceState == null) {
            Fragment fragment = getFragmentForPuzzle(puzzleId);
            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
        }
    }

    private Fragment getFragmentForPuzzle(int puzzleId) {
        switch (puzzleId) {
            case 1:  return new Puzzle1Fragment();
            case 2:  return new Puzzle2Fragment();
            case 3:  return new Puzzle3Fragment();
            case 4:  return new Puzzle4Fragment();
            case 5:  return new Puzzle5Fragment();
            case 6:  return new Puzzle6Fragment();
            case 7:  return new Puzzle7Fragment();
            case 8:  return new Puzzle8Fragment();
            case 9:  return new Puzzle9Fragment();
            case 10: return new Puzzle10Fragment();
            case 11: return new Puzzle11Fragment();
            case 15: return new Puzzle15Fragment();
            case 17: return new Puzzle17Fragment();
            case 20: return new Puzzle20Fragment();
            case 29: return new Puzzle29Fragment();
            default: return null;
        }
    }
}