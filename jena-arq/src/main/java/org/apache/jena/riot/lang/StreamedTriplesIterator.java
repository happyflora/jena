/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package org.apache.jena.riot.lang;

import org.apache.jena.atlas.lib.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * An RDF stream which also provides a triple iterator
 * <p>
 * Behind the scenes this is implemented as a blocking queue, the RDF stream
 * will accept triples while there is capacity and the iterator will block while
 * waiting for further triples
 * </p>
 * <p>
 * This stream silently discards any tuples or quads
 * </p>
 * 
 */
public class StreamedTriplesIterator extends StreamedRDFIterator<Triple>  {

    /**
     * Creates a new streamed triples iterator using the default buffer size of
     * 1000
     * <p>
     * See {@link #StreamedTriplesIterator(int, boolean)} for more discussion of
     * the parameters
     * </p>
     */
    public StreamedTriplesIterator() {
        this(1000, false);
    }

    /**
     * Creates a new streamed triples iterator
     * <p>
     * See {@link #StreamedTriplesIterator(int, boolean)} for more discussion of
     * the parameters
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     */
    public StreamedTriplesIterator(int bufferSize) {
        this(bufferSize, false);
    }

    /**
     * Creates a new streamed triples iterator
     * <p>
     * Buffer size must be chosen carefully in order to avoid performance
     * problems, if you set the buffer size too low you will experience a lot of
     * blocked calls so it will take longer to consume the data from the
     * iterator.  For best performance the buffer size should be at least
     * 10% of the expected input size though you may need to tune this depending
     * on how fast your consumer thread is.
     * </p>
     * <p>
     * The fair parameter controls whether the locking policy used for the
     * buffer is fair. When enabled this reduces throughput but also reduces the
     * chance of thread starvation.
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     * @param fair
     *            Whether the buffer should use a fair locking policy
     */
    public StreamedTriplesIterator(int bufferSize, boolean fair) {
        super(bufferSize, fair);
    }
    
    @Override
    public void triple(Triple triple) {
        if (triple == null)
            return;
        // This may thrown an IllegalStateException if the queue is full
        while (true) {
            try {
                this.buffer.put(triple);
                break;
            } catch (InterruptedException e) {
                // Ignore and retry
            }
        }
    }

    @Override
    public void quad(Quad quad) {
        // Quads are discarded
    }

    @Override
    public void tuple(Tuple<Node> tuple) {
        // Tuples are discarded
    }

}