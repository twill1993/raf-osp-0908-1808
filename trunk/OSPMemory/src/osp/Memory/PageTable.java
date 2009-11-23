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
	static int absPage = 0;
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
    		super.pages[i] = new PageTableEntry(this, ++PageTable.absPage);
    	}

    }

    /**
       Frees up main memory occupied by the task.
       Then unreserves the freed pages, if necessary.

       @OSPProject Memory
    */
    public void do_deallocateMemory()
    {
    	TaskCB task = super.getTask();
    	for(int i = maxPages - 1; i >= 0; --i)
    	{
    		if(pages[i].isValid())
    		{
    			if(pages[i].getFrame().getReserved() == task)
    			{
    				pages[i].getFrame().setReserved(null);
    			}
    			
    			pages[i].getFrame().setDirty(true);
    			pages[i].getFrame().setReferenced(false);
    			pages[i].getFrame().setPage(null);
    		}
    	}
    	
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
