package com.placement.portal.service.recommendation;

import com.placement.portal.domain.enums.ExperienceLevel;
import com.placement.portal.domain.enums.JobType;
import com.placement.portal.domain.enums.ProficiencyLevel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for {@link ScoringEngine}.
 *
 * <p>The engine is stateless and Spring-free, so these tests construct it directly.
 * They protect the CLAUDE.md invariant that a student below the job's minCgpa
 * receives a CGPA score of 0 (hard disqualifier).</p>
 */
class ScoringEngineTest {

    private final ScoringEngine engine = new ScoringEngine();

    @Test
    void skillScore_perfectMandatoryMatch_returns100() {
        double score = engine.computeSkillScore(
                Set.of("java", "spring", "sql"),
                Map.of("java", ProficiencyLevel.ADVANCED, "spring", ProficiencyLevel.INTERMEDIATE),
                Set.of("java", "spring"),
                Set.of("sql")
        );
        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void skillScore_noMatch_returnsZero() {
        double score = engine.computeSkillScore(
                Set.of("python"),
                Map.of(),
                Set.of("java", "spring"),
                Set.of()
        );
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    void cgpaScore_studentBelowMinimum_returnsZero_hardDisqualifier() {
        double score = engine.computeCgpaScore(new BigDecimal("6.0"), new BigDecimal("8.0"));
        assertThat(score)
                .as("CGPA below minimum must be 0 (per CLAUDE.md hard disqualifier rule)")
                .isEqualTo(0.0);
    }

    @Test
    void cgpaScore_studentAtExactMinimum_returns60() {
        double score = engine.computeCgpaScore(new BigDecimal("7.0"), new BigDecimal("7.0"));
        assertThat(score).isEqualTo(60.0);
    }

    @Test
    void cgpaScore_perfectCgpa_returns100() {
        double score = engine.computeCgpaScore(new BigDecimal("10.0"), new BigDecimal("7.0"));
        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void cgpaScore_nullMinimum_returnsNeutral70() {
        double score = engine.computeCgpaScore(new BigDecimal("8.0"), null);
        assertThat(score).isEqualTo(70.0);
    }

    @Test
    void experienceScore_fresherRole_returns100() {
        assertThat(engine.computeExperienceScore(ExperienceLevel.FRESHER, 0)).isEqualTo(100.0);
    }

    @Test
    void preferenceScore_bothMatch_returns100() {
        double score = engine.computePreferenceScore(
                "Bangalore, Pune", "FULL_TIME", "Bangalore", JobType.FULL_TIME);
        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void preferenceScore_neitherMatches_returnsZero() {
        double score = engine.computePreferenceScore(
                "Mumbai", "PART_TIME", "Bangalore", JobType.FULL_TIME);
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    void recencyScore_nullDeadline_returnsNeutral50() {
        assertThat(engine.computeRecencyScore(null)).isEqualTo(50.0);
    }

    @Test
    void recencyScore_oneWeekOut_returns100() {
        assertThat(engine.computeRecencyScore(LocalDate.now().plusDays(7))).isEqualTo(100.0);
    }
}
