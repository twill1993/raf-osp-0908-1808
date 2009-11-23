package osp.Memory;


import osp.Threads.*;
import osp.Devices.*;
import osp.IFLModules.*;
/**
   The PageTableEntry object contains information about a specific virtual
   page in memory, including the page frame in which it resides.
   
   @OSPProject Memory

*/

public class PageTableEntry extends IflPageTableEntry
{
    /**
       The constructor. Must call

       	   super(ownerPageTable,pageNumber);
	   
       as its first statement.

       @OSPProject Memory
    */
    public PageTableEntry(PageTable ownerPageTable, int pageNumber)
    {
    	super(ownerPageTable, pageNumber);
    }

    /**
       This method increases the lock count on the page by one. 

	The method must FIRST increment lockCount, THEN  
	check if the page is valid, and if it is not and no 
	page validation event is present for the page, start page fault 
	by calling PageFaultHandler.handlePageFault().

	@return SUCCESS or FAILURE
	FAILURE happens when the pagefault due to locking fails or the 
	that created the IORB thread gets killed.

	@OSPProject Memory
     */
    
    //goal of this method is to increment the lock count of the fram associated with the page.
    public int do_lock(IORB iorb)
    {
    	//first increment the lock count
    	this.getFrame().incrementLockCount();
    
    	//first check if the page is in main memory by testing the validity bit of the page
    	if(!isValid())
    	{
    		//If the page is invalid, a pagefault must be initiated.
    		PageFaultHandler.handlePageFault(iorb.getThread(), iorb.getDeviceID(), this);
    		return FAILURE;
    	}
    	//To help identify the pages that are involved in a pagefault, OSP 2 provides the method getValidatingThread()
    	//this method returns the thread that caused a pagefault on that page
    	if(iorb.getThread() == null || getValidatingThread() == iorb.getThread())
    	{
    		//If Th2 = Th1, then the proper action is to return right after incrementing the lock count
    		return SUCCESS;
    	}
    	if(iorb.getThread() != null || getValidatingThread() != iorb.getThread()){
    		//If Th2 != Th1 execute the suspend() method on Th2 and pass page P as a parameter
			ThreadCB th = iorb.getThread();
			th.suspend(this);
			if(isValid()){
				return SUCCESS;
				//SUCCESS if the page became valid as a result of the pagefault 
			}
			//and FAILURE otherwise.
			return FAILURE;
		}
    
    	return SUCCESS;
    }

    /** This method decreases the lock count on the page by one. 

	This method must decrement lockCount, but not below zero.

	@OSPProject Memory
    */
    public void do_unlock()
    {
    	//lock count ne sme da padne ispod nule 
    	if(this.getFrame().getLockCount()  > 0)
    	{
    		this.getFrame().decrementLockCount();
    	}

    }


}

