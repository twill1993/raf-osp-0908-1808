package osp.Devices;

/**
    This class stores all pertinent information about a device in
    the device table.  This class should be sub-classed by all
    device classes, such as the Disk class.

    @OSPProject Devices
*/

import osp.Hardware.Disk;
import osp.IFLModules.*;
import osp.Memory.MMU;
import osp.Threads.*;
import osp.Utilities.*;

public class Device extends IflDevice
{
    GenericList iorbWaiting;
    /**
        This constructor initializes a device with the provided parameters.
	As a first statement it must have the following:

	    super(id,numberOfBlocks);

	@param numberOfBlocks -- number of blocks on device

        @OSPProject Devices
    */
    public Device(int id, int numberOfBlocks)
    {
        super(id, numberOfBlocks);
        /**
         * then initialize the device object. 
         * One thing that requires initialization is the
         * TODO: variable iorbQueue described later in this section
         */
         iorbQueue = new GenericList();
         iorbWaiting = new GenericList();
    }

    /**
       This method is called once at the beginning of the
       simulation. Can be used to initialize static variables.

       @OSPProject Devices
    */
    public static void init()
    {

    }

    /**
       Enqueues the IORB to the IORB queue for this device
       according to some kind of scheduling algorithm.
       
       This method must lock the page (which may trigger a page fault),
       check the device's state and call startIO() if the 
       device is idle, otherwise append the IORB to the IORB queue.

       @return SUCCESS or FAILURE.
       FAILURE is returned if the IORB wasn't enqueued 
       (for instance, locking the page fails or thread is killed).
       SUCCESS is returned if the IORB is fine and either the page was 
       valid and device started on the IORB immediately or the IORB
       was successfully enqueued (possibly after causing pagefault pagefault)
       
       @OSPProject Devices
    */
    public int do_enqueueIORB(IORB iorb)
    {
    	//zakljucati stranicu 
    	iorb.getPage().lock(iorb);
    	//povecati iorb count
    	if(iorb.getThread().getStatus() != ThreadKill){
    		iorb.getOpenFile().incrementIORBCount();	
    	}
    	//postaviti cilindar 
    	int blocksPerTrack = ((Disk) this).getSectorsPerTrack()*((Disk) this).getBytesPerSector()/
    						(int) Math.pow(2, MMU.getVirtualAddressBits() - MMU.getPageAddressBits()); 
    	int cylinder = iorb.getBlockNumber()/ (blocksPerTrack * ((Disk) this).getPlatters());
    	iorb.setCylinder(cylinder);
    	
    	if(iorb.getThread().getStatus() == ThreadCB.ThreadKill)
    	{
    		return FAILURE;
    	}
    	else
    	{
    		if(!this.isBusy())
    		{
    			startIO(iorb);
    			return SUCCESS;
    		}
    		else
    		{
    			((GenericList) iorbQueue).insert(iorb);
    			return SUCCESS;
    		}
    	}
    }

    /**
       Selects an IORB (according to some scheduling strategy)
       and dequeues it from the IORB queue.

       @OSPProject Devices
    */
    public IORB do_dequeueIORB()
    {
    	if(iorbQueue.isEmpty())
    	{
    		return null;
    	}
    	else
    	{
    		IORB iorb = (IORB) ((GenericList) iorbQueue).removeTail();
    		//((GenericList) iorbQueue).remove(iorb);
    		return iorb;
    	}
    }

    /**
        Remove all IORBs that belong to the given ThreadCB from 
	this device's IORB queue

        The method is called when the thread dies and the I/O 
        operations it requested are no longer necessary. The memory 
        page used by the IORB must be unlocked and the IORB count for 
	the IORB's file must be decremented.

	@param thread thread whose I/O is being canceled

        @OSPProject Devices
    */
    public void do_cancelPendingIO(ThreadCB thread)
    {
    	//System.out.println(thread.getStatus() == ThreadKill);
    	if(iorbQueue.isEmpty())
    	{
    		return;
    	}

    	for(int i = iorbQueue.length() - 1; i >= 0; i--) 
    	{
    		IORB iorb = (IORB) ((GenericList) iorbQueue).getAt(i);
    		if(iorb.getThread().equals(thread))// && thread.getStatus() == ThreadCB.ThreadKill)
    		{
    			//unlock the page associated with the thread
    			iorb.getPage().unlock();
    		
    			//decrement iorb count
    			iorb.getOpenFile().decrementIORBCount();
    			
    			//try closing open file handle
    	
    				if(iorb.getOpenFile().getIORBCount() == 0 && iorb.getOpenFile().closePending)
    				{
    					iorb.getOpenFile().close();	
    				}
    				((GenericList) iorbQueue).remove(iorb);
    			
    		}
    	}
    }

    /** Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures
	in their state just after the error happened.  The body can be
	left empty, if this feature is not used.
	
	@OSPProject Devices
     */
    public static void atError()
    {
     Device.printableStatus(getTableSize());
     System.out.println("Error");
    }

    /** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.
	
	@OSPProject Devices
     */
    public static void atWarning()
    {
    	System.out.println("Warning");
    }

}

