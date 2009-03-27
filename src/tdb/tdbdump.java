/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import tdb.cmdline.CmdSub;
import tdb.cmdline.CmdTDB;
import tdb.cmdline.ModFormat;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.store.FactoryGraphTDB;

public class tdbdump extends CmdSub
{
    ModFormat modFormat =  new ModFormat() ;
    
    static public void main(String... argv)
    { 
        CmdUtils.setLog4j() ;
        new tdbdump(argv).exec() ;
    }

//    protected tdbdump(String[] argv)
//    {
//        super(argv) ;
//        super.addModule(modFormat) ;
//    }
//
//    @Override
//    protected String getSummary()
//    {
//        return Utils.className(this)+" --desc=DIR [--format=FORMAT]" ;
//    }
//
//    @Override
//    protected String getCommandName()
//    {
//        return "tdbdump" ;
//    }
//
//    @Override
//    protected void exec()
//    {
//        Model model = getModel() ;
//        String format = modFormat.getFormat("N3-TRIPLES") ;
//        model.write(System.out, format) ;
//    }

        static final String CMD_DATA =     "data" ; 
        static final String CMD_INDEX =     "index" ; 
        
        protected tdbdump(String...argv)
        {
            super(argv) ;
            super.addSubCommand(CMD_INDEX, new Exec()
            { @Override public void exec(String[] argv) { new SubIndex(argv).mainRun() ; } }) ;
            super.addSubCommand(CMD_DATA, new Exec()
            { @Override public void exec(String[] argv) { new SubData(argv).mainRun() ; } }) ;
        }
        

    class SubData extends CmdTDB
    {
        protected SubData(String... argv)
        {
            super(argv) ;
        }

        @Override
        protected String getSummary()
        {
            return null ;
        }

        @Override
        protected void exec()
        {
            Model model = getModel() ;
            String format = modFormat.getFormat("N3-TRIPLES") ;
            model.write(System.out, format) ;
        }
    }
    
    static class SubIndex extends CmdTDB
    {
        protected SubIndex(String[] argv)
        {
            super(argv) ;
        }

        @Override
        protected String getSummary()
        {
            return "tdbdump index INDEX" ;
        }

        @Override
        protected void exec()
        {
            for ( String fn: super.getPositional() )
            {
                execOne(fn) ;
            }
        }
        
        private void execOne(String fn)
        {
            FileSet fileset = new FileSet("DB", "SPO") ;
            RangeIndex rIndex = IndexBuilder.createRangeIndex(fileset, FactoryGraphTDB.indexRecordTripleFactory) ;
            for ( Record r : rIndex )
            {
                System.out.println(r.toString()) ;
            }
        }
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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