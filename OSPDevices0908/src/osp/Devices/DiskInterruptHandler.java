package osp.Devices;
import java.util.*;
import osp.IFLModules.*;
import osp.Hardware.*;
import osp.Interrupts.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Tasks.*;
import osp.Memory.*;
import osp.FileSys.*;

/**
    The disk interrupt handler.  When a disk I/O interrupt occurs,
    this class is called upon the handle the interrupt.

    @OSPProject Devices
*/
public class DiskInterruptHandler extends IflDiskInterruptHandler
{
    /** 
        Handles disk interrupts. 
        
        This method obtains the interrupt parameters from the 
        interrupt vector. The parameters are IORB that caused the 
        interrupt: (IORB)InterruptVector.getEvent(), 
        and thread that initiated the I/O operation: 
        InterruptVector.getThread().
        The IORB object contains references to the memory page 
        and open file object that participated in the I/O.
        
        The method must unlock the page, set its IORB field to null,
        and decrement the file's IORB count.
        
        The method must set the frame as dirty if it was memory write 
        (but not, if it was a swap-in, check whether the device was 
        SwapDevice)

        As the last thing, all threads that were waiting for this 
        event to finish, must be resumed.

        @OSPProject Devices 
    */
    public void do_handleInterrupt()
    {
        IORB iorb = (IORB) InterruptVector.getEvent();
        iorb.getOpenFile().decrementIORBCount();
        if(iorb.getOpenFile().closePending && iorb.getOpenFile().getIORBCount() == 0)
        	iorb.getOpenFile().close();

        iorb.getPage().unlock();
        
        if(iorb.getThread().getStatus() == ThreadKill)
        	return;
        
        if(iorb.getDeviceID() != SwapDeviceID)
        {
	        iorb.getPage().getFrame().setReferenced(true);
	        if(iorb.getIOType() == MemoryWrite)
	        	iorb.getPage().getFrame().setDirty(true);
        }
        else
        {
        	if(iorb.getThread().getStatus() != ThreadKill)
        		iorb.getPage().getFrame().setDirty(false);
        }
        
        if(iorb.getThread().getTask().getStatus() != TaskLive)
        	iorb.getPage().getFrame().setUnreserved(iorb.getThread().getTask());
        
        iorb.notifyThreads();
        Device dev = Device.get(iorb.getDeviceID()); 
        dev.setBusy(false);
        
        iorb = dev.dequeueIORB();
        if(iorb != null)
        {
        	dev.startIO(iorb);
        }
        
        ThreadCB.dispatch();
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
