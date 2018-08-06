/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class AidoVehicleStatistic {

    private int lastLinkIndex = -1;
    // this is used as a buffer and is periodically emptied
    private final List<VehicleContainer> list = new LinkedList<>();

    public AidoVehicleStatistic() {
    }

    public Tensor register(int simObjIndex, VehicleContainer vehicleContainer) {
        Tensor distance = Tensors.of(Quantity.of(0, SI.METER), Quantity.of(0, SI.METER));
        if (vehicleContainer.linkIndex != lastLinkIndex) {
            distance = consolidate();
            list.clear();
            lastLinkIndex = vehicleContainer.linkIndex;
        }
        list.add(vehicleContainer);
        return distance;
    }

    /** this function is called when the {@link RoboTaxi} has changed the link, then we can
     * register the distance covered by the vehicle on the previous link and associate it to
     * timesteps. The logic is that the distance is added evenly to the time steps. */
    public Tensor consolidate() {
        Scalar distDrive = Quantity.of(0, SI.METER);
        Scalar distEmpty = Quantity.of(0, SI.METER);
        if (!list.isEmpty()) {
            final int linkId = list.get(0).linkIndex;
            Link distanceLink = MatsimStaticDatabase.INSTANCE.getOsmLink(linkId).link;
            /** this total distance on the link was travelled on during all simulationObjects stored
             * in the list. */
            Scalar distance = Quantity.of(distanceLink.getLength(), SI.METER);

            int part = Math.toIntExact(list.stream().filter(vc -> vc.roboTaxiStatus.isDriving()).count());
            Scalar stepDistcontrib = distance.divide(RationalScalar.of(part, 1));

            for (VehicleContainer vehicleContainer : list) {
                switch (vehicleContainer.roboTaxiStatus) {
                case DRIVEWITHCUSTOMER:
                    distDrive = distDrive.add(stepDistcontrib);
                    break;
                case DRIVETOCUSTOMER:
                    distEmpty = distEmpty.add(stepDistcontrib);
                    break;
                case REBALANCEDRIVE:
                    distEmpty = distEmpty.add(stepDistcontrib);
                    break;
                default:
                    break;
                }
            }
        }
        return Tensors.of(distDrive, distEmpty);
    }
}
