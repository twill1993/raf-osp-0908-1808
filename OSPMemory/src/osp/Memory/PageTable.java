package osp.Memory;
/**
    The PageTable class represents the page table for a given task.
    A PageTable consists of an array of PageTableEntry objects.  This
    page table is of the non-inverted type.

    @OSPProject Memory
*/
import java.lang.Math;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Hardware.*;

public class PageTable extends IflPageTable
{
	int maxPages = 0;
    /** 
	The page table constructor. Must call
	
	    super(ownerTask)

	as its first statement.

	@OSPProject Memory
    */
    public PageTable(TaskCB ownerTask)
    {
    	super(ownerTask);
    	
    	maxPages = (int) Math.pow(2, MMU.getPageAddressBits()); // Broj stranica
    	super.pages = new PageTableEntry[maxPages]; // Inicijalizacija arraya
    	
    	for(int i = 0; i < maxPages; ++i) // Konstruisanje svake, dijeli im se po redni broj
    	{
    		super.pages[i] = new PageTableEntry(this, i);
    	}

    }

    /**
       Frees up main memory occupied by the task.
       Then unreserves the freed pages, if necessary.

       @OSPProject Memory
    */
    public void do_deallocateMemory()
    {
        // your code goes here
    	//this.pages.length
//    	TaskCB task = super.getTask();
//    	
//    	MMU.getFrame(0).
//    	for(int i = 0; i < task.getPa; ++i)
//    	{
//    		super.pages[i] = null;
//    		MMU.getf
//    	}
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
