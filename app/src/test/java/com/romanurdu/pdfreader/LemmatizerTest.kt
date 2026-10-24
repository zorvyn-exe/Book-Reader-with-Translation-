package com.romanurdu.pdfreader

import com.romanurdu.pdfreader.dictionary.Lemmatizer
import org.junit.Assert.assertTrue
import org.junit.Test

class LemmatizerTest {

    @Test
    fun testPluralNounLemmatization() {
        val candidates1 = Lemmatizer.getCandidateLemmas("books")
        assertTrue("Candidate lemmas for 'books' should contain 'book'", candidates1.contains("book"))

        val candidates2 = Lemmatizer.getCandidateLemmas("watches")
        assertTrue("Candidate lemmas for 'watches' should contain 'watch'", candidates2.contains("watch"))

        val candidates3 = Lemmatizer.getCandidateLemmas("cities")
        assertTrue("Candidate lemmas for 'cities' should contain 'city'", candidates3.contains("city"))
    }

    @Test
    fun testPastTenseLemmatization() {
        val candidates1 = Lemmatizer.getCandidateLemmas("walked")
        assertTrue("Candidate lemmas for 'walked' should contain 'walk'", candidates1.contains("walk"))

        val candidates2 = Lemmatizer.getCandidateLemmas("cried")
        assertTrue("Candidate lemmas for 'cried' should contain 'cry'", candidates2.contains("cry"))

        val candidates3 = Lemmatizer.getCandidateLemmas("stopped")
        assertTrue("Candidate lemmas for 'stopped' should contain 'stop'", candidates3.contains("stop"))
    }

    @Test
    fun testContinuousFormLemmatization() {
        val candidates1 = Lemmatizer.getCandidateLemmas("running")
        assertTrue("Candidate lemmas for 'running' should contain 'run'", candidates1.contains("run"))

        val candidates2 = Lemmatizer.getCandidateLemmas("reading")
        assertTrue("Candidate lemmas for 'reading' should contain 'read'", candidates2.contains("read"))

        val candidates3 = Lemmatizer.getCandidateLemmas("making")
        assertTrue("Candidate lemmas for 'making' should contain 'make'", candidates3.contains("make"))
    }

    @Test
    fun testAdverbLemmatization() {
        val candidates1 = Lemmatizer.getCandidateLemmas("happily")
        assertTrue("Candidate lemmas for 'happily' should contain 'happy'", candidates1.contains("happy"))

        val candidates2 = Lemmatizer.getCandidateLemmas("quickly")
        assertTrue("Candidate lemmas for 'quickly' should contain 'quick'", candidates2.contains("quick"))
    }

    @Test
    fun testIrregularForms() {
        val candidates1 = Lemmatizer.getCandidateLemmas("went")
        assertTrue("Candidate lemmas for 'went' should contain 'go'", candidates1.contains("go"))

        val candidates2 = Lemmatizer.getCandidateLemmas("children")
        assertTrue("Candidate lemmas for 'children' should contain 'child'", candidates2.contains("child"))
    }
}
