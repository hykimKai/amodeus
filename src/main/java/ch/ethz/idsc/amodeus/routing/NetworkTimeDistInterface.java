/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.tensor.Scalar;

// TODO can we put this together with the {@link DistanceFunction} inteerface? No need for redundancy...
public interface NetworkTimeDistInterface {

    public Scalar travelTime(Link from, Link to, Double now);

    public Scalar distance(Link from, Link to, Double now);

}