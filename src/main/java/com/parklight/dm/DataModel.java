package com.parklight.dm;

import java.io.Serializable;

/**
 * Marker interface for all data models in the system.
 * Lets generic code (like the network Request) accept any model type
 * without knowing the concrete class.
 */
public interface DataModel extends Serializable {
}
