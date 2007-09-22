/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import com.hp.hpl.jena.sparql.core.Quad;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlDistinct;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRename;

public class QuadBlockCompilerMain implements QuadBlockCompiler
{
    SDBRequest request ;
    SlotCompiler slotCompiler ;
    static public PatternTable patternTable = null ;

    public QuadBlockCompilerMain(SDBRequest request, SlotCompiler slotCompiler)
    {
        //super(request, slotCompiler) ;
        this.request = request ;
        this.slotCompiler = slotCompiler ;
    }
    
    public SlotCompiler getSlotCompiler()
    { return slotCompiler ; }

    public static boolean attemptMerge = false ; 
    
    //@Override
    public SqlNode compile(QuadBlock quads)
    {
        SqlNode sqlNode = slotCompiler.start(quads) ;
        quads = new QuadBlock(quads) ;          // Copy it because it's modified.

        // ---- Stage builder 
        SqlStageList sList = new SqlStageList() ;
        // Potential concurrent modification - need to use an explicit index.
        for ( int i = 0 ; i < quads.size() ; )
        {
            Quad q = quads.get(i) ;
            if ( patternTable != null && patternTable.trigger(q) )
            {
                // Removes current quad
                SqlStage stage = patternTable.process(i, quads) ;
                if ( stage != null )
                {
                    if ( quads.get(i) == q )
                        throw new SDBException("Pattern table returned a stage but did not remove the first quad") ;
                    sList.add(stage) ;
                    continue ;
                }
            }
            sList.add(new SqlStageBasicQuad(q)) ; 
            i++ ;
        }

        // ---- and now turn the stages into SqlNodes  
        SqlNode sqlStages = sList.build(request, slotCompiler) ;
        
        // --- Join the initial node (constants). 
        sqlNode = QC.innerJoin(request, sqlNode, sqlStages) ;
        sqlNode = slotCompiler.finish(sqlNode, quads) ;
        
        // Insert DISTINCT if accessing the RDF merge of all named graphs
        // An RDF Merge is the DISTINCT results of query over the union of all graphs.
        // Or in TransformSDB

        boolean needDistinct = false ;
        if ( quads.getGraphNode().equals(Quad.unionGraph) )
            needDistinct = true ;
        
        if ( needDistinct && attemptMerge )
        {
            // DISTINCT -- over the names variables but not * (which includes the graph node).
            
            // Get a single table.  This projects only the variables, not the unnamed graph node column. 
            sqlNode = SqlRename.view("A", sqlNode) ;
            sqlNode = SqlDistinct.distinct(sqlNode) ;
        }
        
        return sqlNode ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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