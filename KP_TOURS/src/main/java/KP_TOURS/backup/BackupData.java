package KP_TOURS.backup;


import KP_TOURS.model.Trip;

import java.util.ArrayList;
import java.util.List;

public class BackupData {

    private List<Trip> trips =
            new ArrayList<>();

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }
}