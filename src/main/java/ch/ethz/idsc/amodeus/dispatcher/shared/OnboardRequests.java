package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum OnboardRequests {
    ;
    
    public static Set<AVRequest> getOnBoardRequests(List<? extends SharedCourse> courses) {
        Set<AVRequest> pickups = getRequestsWithMealType(courses, SharedMealType.PICKUP);
        Set<AVRequest> dropoffs = getRequestsWithMealType(courses, SharedMealType.DROPOFF);
        for (AVRequest avRequestIDpickup : pickups) {
            boolean removeOk = dropoffs.remove(avRequestIDpickup);
            GlobalAssert.that(removeOk);
        }
        GlobalAssert.that(getNumberCustomersOnBoard(courses) == dropoffs.size());
        return dropoffs;
    }

    
    private static Set<AVRequest> getRequestsWithMealType(List<? extends SharedCourse> courses, SharedMealType sharedMealType) {
        return courses.stream().filter(sc -> sc.getMealType().equals(sharedMealType)).map(sc -> sc.getAvRequest()).collect(Collectors.toSet());
    }

    
    public static long getNumberCustomersOnBoard(List<? extends SharedCourse> courses) {
        return getNumberDropoffs(courses) - getNumberPickups(courses);
    }

    
    public static long getNumberPickups(List<? extends SharedCourse> courses) {
        return getNumberSharedMealType(courses, SharedMealType.PICKUP);
    }

    public static long getNumberDropoffs(List<? extends SharedCourse> courses) {
        return getNumberSharedMealType(courses, SharedMealType.DROPOFF);
    }
    
    private static long getNumberSharedMealType(List<? extends SharedCourse> courses, SharedMealType sharedMealType) {
        return courses.stream().filter(sc -> sc.getMealType().equals(sharedMealType)).count();
    }
    
    public static long getNumberRedirections(List<? extends SharedCourse> courses) {
        return getNumberSharedMealType(courses, SharedMealType.REDIRECT);
    }
    
    
    public static boolean canPickupNewCustomer(RoboTaxi roboTaxi) {
        int onBoard = getNumberOnBoardRequests(roboTaxi);
        GlobalAssert.that(onBoard >= 0);
        return onBoard < roboTaxi.getCapacity();
    }

    public static Set<AVRequest> getAvRequestsOnBoard(RoboTaxi roboTaxi) {
        return OnboardRequests.getOnBoardRequests(roboTaxi.getUnmodifiableViewOfCourses());
    }

    public static int getNumberOnBoardRequests(RoboTaxi roboTaxi) {
        return (int) OnboardRequests.getNumberCustomersOnBoard(roboTaxi.getUnmodifiableViewOfCourses());
    }


}
