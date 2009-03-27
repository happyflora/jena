/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Iterator;

import lib.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.util.FmtUtils;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.graph.GraphSyncListener;
import com.hp.hpl.jena.tdb.graph.UpdateListener;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** A graph implementation that uses a triple table - free-standing graph or deafult graph of dataset */
public class GraphTriplesTDB extends GraphTDBBase
{
    private static Logger log = LoggerFactory.getLogger(GraphTriplesTDB.class) ;
    
    private final TripleTable tripleTable ;
    private final DatasetPrefixes prefixes ;
    
    public GraphTriplesTDB(TripleTable tripleTable,
                           DatasetPrefixes prefixes,
                           ReorderTransformation reorderTransform,
                           Location location)
    {
        super(reorderTransform, location) ;
        
        this.tripleTable = tripleTable ;
        this.prefixes = prefixes ;
        
        int syncPoint = SystemTDB.SyncTick ;
        if ( syncPoint > 0 )
            this.getEventManager().register(new GraphSyncListener(this, syncPoint)) ;
        this.getEventManager().register(new UpdateListener(this)) ;
    }
    
    @Override
    public void performAdd( Triple t ) 
    { 
        boolean changed = tripleTable.add(t) ;
        if ( ! changed )
            duplicate(t) ;
    }

    private void duplicate(Triple t)
    {
        if ( TDB.getContext().isTrue(SystemTDB.symLogDuplicates) && log.isInfoEnabled() )
        {
            String $ = FmtUtils.stringForTriple(t, this.getPrefixMapping()) ;
            log.info("Duplicate: ("+$+")") ;
        }
    }

    @Override
    public void performDelete( Triple t ) 
    { 
        boolean changed = tripleTable.delete(t) ;
    }
    
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
    {
        Iterator<Triple> iter = tripleTable.find(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
        if ( iter == null )
            return com.hp.hpl.jena.util.iterator.NullIterator.instance() ;
        return new MapperIteratorTriples(iter) ;
    }

//    @Override
//    public boolean isEmpty()        { return tripleTable.isEmpty() ; }
    
    // B+Trees don't (yet) have a proper size
//    @Override
//    public int graphBaseSize()
//    {
//        return (int)tripleTable.size() ;
//    }
        

    @Override
    public Tuple<Node> asTuple(Triple triple)
    {
        return Tuple.create(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }
    
    @Override
    protected Iterator<Tuple<NodeId>> countThis()
    {
        return tripleTable.getNodeTupleTable().getTupleTable().getIndex(0).all() ;
    }

    @Override
    public NodeTupleTable getNodeTupleTable()           { return tripleTable.getNodeTupleTable()   ; }
    
    public final Node getGraphNode()                    { return null ; }
    
    @Override
    protected PrefixMapping createPrefixMapping()
    {
        return prefixes.getPrefixMapping() ;
    }

    @Override
    final public void close()
    {
        tripleTable.close();
        super.close() ;
    }
    
    @Override
    public void sync(boolean force)
    {
        prefixes.sync(force) ;
        tripleTable.sync(force);
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */