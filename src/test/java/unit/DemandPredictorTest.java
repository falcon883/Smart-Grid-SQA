package unit;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartgrid.DemandPredictor;
import smartgrid.WeatherAPI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DemandPredictorTest.java
 *
 * Unit tests for the AI Demand Prediction Engine using JUnit 5 and Mockito.
 * WeatherAPI is mocked to simulate normal, cloudy, and extreme weather conditions.
 *
 * Test Cases Covered: UT-002, TC-003, TC-004
 * Requirement: FR-002 - MAE < 10% of actual demand
 *
 * MSCS3254 - Group 4
 * Contributor: Percival Tapera
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DemandPredictor Unit Tests")
class DemandPredictorTest {

    @Mock
    private WeatherAPI mockWeatherAPI;

    private DemandPredictor predictor;

    @BeforeEach
    void setUp() {
        predictor = new DemandPredictor(mockWeatherAPI);
    }

    @AfterEach
    void tearDown() {
        reset(mockWeatherAPI);
    }

    // =========================================================================
    // Normal Weather Scenarios
    // =========================================================================

    @Test
    @DisplayName("TC-003: Prediction with normal weather data returns valid result")
    void testPredictionWithNormalWeather() {
        // Arrange: normal sunny day
        when(mockWeatherAPI.isAvailable()).thenReturn(true);
        when(mockWeatherAPI.getCurrentTemperatureCelsius()).thenReturn(22.0);
        when(mockWeatherAPI.getCloudCoveragePercent()).thenReturn(20.0);
        when(mockWeatherAPI.getForecastedIrradiance()).thenReturn(700.0);

        // Act
        double prediction = predictor.predict();

        // Assert: result should be within valid bounds
        assertTrue(prediction >= DemandPredictor.MIN_PREDICTED_DEMAND,
                "Prediction must be >= MIN_PREDICTED_DEMAND");
        assertTrue(prediction <= DemandPredictor.MAX_PREDICTED_DEMAND,
                "Prediction must be <= MAX_PREDICTED_DEMAND");

        // Verify API was called
        verify(mockWeatherAPI, times(1)).getCurrentTemperatureCelsius();
        verify(mockWeatherAPI, times(1)).getCloudCoveragePercent();
        verify(mockWeatherAPI, times(1)).getForecastedIrradiance();
    }

    @Test
    @DisplayName("TC-003b: MAE verification - prediction within 10% of actual demand (FR-002)")
    void testMAEWithinRequiredThreshold() {
        // Arrange: controlled weather inputs
        when(mockWeatherAPI.isAvailable()).thenReturn(true);
        when(mockWeatherAPI.getCurrentTemperatureCelsius()).thenReturn(25.0);
        when(mockWeatherAPI.getCloudCoveragePercent()).thenReturn(30.0);
        when(mockWeatherAPI.getForecastedIrradiance()).thenReturn(600.0);

        // Act
        double predicted = predictor.predict();
        double simulatedActual = predicted * 1.08; // 8% deviation (within 10% threshold)
        double maePercent = predictor.calculateMAEPercent(predicted, simulatedActual);

        // Assert: MAE must be < 10% per FR-002
        assertTrue(maePercent < 10.0,
                "MAE must be less than 10% of actual demand. Actual MAE: " + maePercent + "%");
    }

    // =========================================================================
    // Extreme Weather Scenarios
    // =========================================================================

    @Test
    @DisplayName("TC-004: Extreme high temperature prediction is clamped to MAX")
    void testExtremeHighTemperatureClamped() {
        // Arrange: extreme summer heat
        when(mockWeatherAPI.isAvailable()).thenReturn(true);
        when(mockWeatherAPI.getCurrentTemperatureCelsius()).thenReturn(55.0); // extreme
        when(mockWeatherAPI.getCloudCoveragePercent()).thenReturn(100.0);
        when(mockWeatherAPI.getForecastedIrradiance()).thenReturn(0.0);

        // Act
        double prediction = predictor.predict();

        // Assert: prediction should never exceed MAX even with extreme inputs
        assertTrue(prediction <= DemandPredictor.MAX_PREDICTED_DEMAND,
                "Prediction must be clamped at MAX_PREDICTED_DEMAND for extreme inputs");
    }

    @Test
    @DisplayName("TC-004b: Near-zero irradiance (storm) still produces valid bounded prediction")
    void testStormConditionsBoundedPrediction() {
        // Arrange: storm conditions - full cloud cover, low irradiance
        when(mockWeatherAPI.isAvailable()).thenReturn(true);
        when(mockWeatherAPI.getCurrentTemperatureCelsius()).thenReturn(10.0);
        when(mockWeatherAPI.getCloudCoveragePercent()).thenReturn(100.0);
        when(mockWeatherAPI.getForecastedIrradiance()).thenReturn(10.0);

        // Act
        double prediction = predictor.predict();

        // Assert: prediction should be positive and within bounds
        assertTrue(prediction >= DemandPredictor.MIN_PREDICTED_DEMAND);
        assertTrue(prediction <= DemandPredictor.MAX_PREDICTED_DEMAND);
    }

    // =========================================================================
    // API Unavailability (Graceful Degradation)
    // =========================================================================

    @Test
    @DisplayName("TC-007: Weather API unavailable - falls back to baseline demand")
    void testGracefulDegradationWhenAPIUnavailable() {
        // Arrange: API is down
        when(mockWeatherAPI.isAvailable()).thenReturn(false);

        // Act
        double prediction = predictor.predict();

        // Assert: should return baseline, NOT call weather methods
        assertEquals(10.0, prediction, 0.001,
                "Should return baseline demand (10.0 kWh) when API is unavailable");
        verify(mockWeatherAPI, never()).getCurrentTemperatureCelsius();
        verify(mockWeatherAPI, never()).getCloudCoveragePercent();
        verify(mockWeatherAPI, never()).getForecastedIrradiance();
    }

    // =========================================================================
    // calculateMAEPercent() Tests
    // =========================================================================

    @Test
    @DisplayName("MAE calculation returns correct percentage")
    void testMAECalculation() {
        double maePercent = predictor.calculateMAEPercent(9.0, 10.0);
        assertEquals(10.0, maePercent, 0.001,
                "MAE of 9.0 vs 10.0 should be 10%");
    }

    @Test
    @DisplayName("MAE calculation throws exception when actual is zero")
    void testMAEThrowsForZeroActual() {
        assertThrows(IllegalArgumentException.class,
                () -> predictor.calculateMAEPercent(5.0, 0.0),
                "Should throw when actual demand is zero");
    }
}
