package osp.Devices;
import osp.IFLModules.*;
import osp.Interrupts.*;
import osp.Threads.*;
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
    	//1.
    	IORB iorb = (IORB) InterruptVector.getEvent();
        ThreadCB thread = InterruptVector.getThread();
      //  PageTableEntry page = iorb.getPage();
        OpenFile oFile = iorb.getOpenFile();
      
       //2.
        oFile.decrementIORBCount();
       
        //3.
        if(oFile.closePending && iorb.getOpenFile().getIORBCount() == 0)
        {
    	   oFile.close();
        }
        
        //4.
        iorb.getPage().do_lock(iorb);
       
        //5.
        if(iorb.getThread().getTask().getStatus() != TaskTerm)
        {
        	if(iorb.getDeviceID() != SwapDeviceID && iorb.getThread().getStatus() == ThreadCB.ThreadKill)
            {
            	iorb.getPage().getFrame().setReferenced(true);
            	if(iorb.getIOType() == FileRead)
            	{
            		iorb.getPage().getFrame().setDirty(true);
            	}
            }
        	//6.
            else
            {
            	iorb.getPage().getFrame().setDirty(false);
            }
        }
        //7.
        if(iorb.getThread().getTask().getStatus() == TaskTerm && iorb.getPage().getFrame().isReserved())
        {
        	iorb.getPage().getFrame().setUnreserved(iorb.getThread().getTask());
        }
        //8.
        iorb.notifyThreads();
        
        //9.
        int id = iorb.getDeviceID();
        Device d = Device.get(id);
        Device.get(id).setBusy(false);
        
        //10.
        if(d.dequeueIORB() != null){
        	d.startIO(iorb);
        }
      
        //11. 
        ThreadCB.dispatch();
    }


}

