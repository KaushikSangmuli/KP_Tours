package KP_TOURS.cache;

import KP_TOURS.model.Trip;
import KP_TOURS.model.TripStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TripCacheManager {

    private static final ObservableList<Trip> tripCache =
            FXCollections.observableArrayList();

    private TripCacheManager() {}

    public static void initialize(List<Trip> trips) {
        tripCache.clear();
        if (trips != null) tripCache.addAll(trips);
    }

    public static ObservableList<Trip> getTripCache() {
        return tripCache;
    }

    public static void addTrip(Trip trip) {
        if (trip == null) return;
        tripCache.add(trip);
    }

    public static void updateTrip(Trip updatedTrip) {
        if (updatedTrip == null) return;
        for (int i = 0; i < tripCache.size(); i++) {
            // ✅ Match by UUID (String), not id (Integer)
            if (tripCache.get(i).getUuid().equals(updatedTrip.getUuid())) {
                updatedTrip.touch();
                tripCache.set(i, updatedTrip);
                return;
            }
        }
    }

    public static void removeTrip(String tripUuid) {
        // ✅ Match by UUID consistently
        tripCache.removeIf(trip -> trip.getUuid().equals(tripUuid));
    }

    // ✅ Find by UUID — used by PurchaseSalesListView after edit
    public static Optional<Trip> findByUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) return Optional.empty();
        return tripCache.stream()
                .filter(trip -> uuid.equals(trip.getUuid()))
                .findFirst();
    }

    /**
     * @deprecated Use findByUuid(String) instead.
     * Kept for backwards compatibility — searches by uuid string.
     */
    @Deprecated
    public static Optional<Trip> findById(String id) {
        return findByUuid(id);
    }

    public static FilteredList<Trip> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new FilteredList<>(tripCache, trip -> true);
        }
        String searchText = keyword.toLowerCase().trim();
        return new FilteredList<>(tripCache, trip ->
                contains(trip.getName(), searchText)
                        || contains(trip.getSector(), searchText)
                        || contains(trip.getAirlineName(), searchText)
                        || contains(trip.getBookedBy(), searchText)
                        || contains(trip.getPnrNo(), searchText)
                        || contains(trip.getStatus() != null
                        ? trip.getStatus().name() : "", searchText)
        );
    }

    public static List<Trip> getTripsByDate(LocalDate date) {
        return tripCache.stream()
                .filter(trip -> trip.getTripDate() != null
                        && trip.getTripDate().equals(date))
                .collect(Collectors.toList());
    }

    public static List<Trip> getTripsByMonth(YearMonth yearMonth) {
        return tripCache.stream()
                .filter(trip -> trip.getTripDate() != null
                        && YearMonth.from(trip.getTripDate()).equals(yearMonth))
                .collect(Collectors.toList());
    }

    public static List<Trip> getTripsByStatus(TripStatus status) {
        return tripCache.stream()
                .filter(trip -> trip.getStatus() == status)
                .collect(Collectors.toList());
    }

    public static int getTotalTrips(YearMonth yearMonth) {
        return getTripsByMonth(yearMonth).size();
    }

    public static double getTotalSellAmount(YearMonth yearMonth) {
        return getTripsByMonth(yearMonth).stream()
                .mapToDouble(Trip::getSellAmount).sum();
    }

    public static double getTotalPurchaseAmount(YearMonth yearMonth) {
        return getTripsByMonth(yearMonth).stream()
                .mapToDouble(Trip::getPurchaseAmount).sum();
    }

    public static double getTotalProfit(YearMonth yearMonth) {
        return getTripsByMonth(yearMonth).stream()
                .mapToDouble(Trip::getProfit).sum();
    }

    public static long getStatusCount(YearMonth yearMonth, TripStatus status) {
        return getTripsByMonth(yearMonth).stream()
                .filter(trip -> trip.getStatus() == status)
                .count();
    }

    public static void clearCache() {
        tripCache.clear();
    }

    public static int size() {
        return tripCache.size();
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}