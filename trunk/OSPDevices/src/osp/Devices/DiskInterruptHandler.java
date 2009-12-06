package osp.Devices;
import osp.IFLModules.*;
import osp.Interrupts.*;
import osp.Tasks.TaskCB;
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
        OpenFile oFile = iorb.getOpenFile();
        ThreadCB thread = iorb.getThread();
        PageTableEntry page = iorb.getPage();
        FrameTableEntry frame = page.getFrame();
        
       //2.
        oFile.decrementIORBCount();
        
      
      	//3.
    	if(oFile.getIORBCount() == 0 && oFile.closePending)
		{
			oFile.close();	
		}
			
        //4.
        iorb.getPage().unlock();	
        
		
        //5.
        TaskCB task = thread.getTask();
        if(task.getStatus() != TaskTerm)
        {
        	if(iorb.getDeviceID() != SwapDeviceID && thread.getStatus() != ThreadCB.ThreadKill)
            {
            	frame.setReferenced(true);
            	if(iorb.getIOType() == FileRead)
            	{
            		frame.setDirty(true);
            	}
            }
        	//6.
        	else //if(iorb.getDeviceID() == SwapDeviceID)
            {
            	frame.setDirty(false);
            }
        }
        //7.
        if(task.getStatus() == TaskTerm && frame.isReserved())
        {
        	frame.setUnreserved(task);
        }
        //8.
        iorb.notifyThreads();
        
        //9.
        int deviceID = iorb.getDeviceID();
        Device.get(deviceID).setBusy(false);
        IORB device = Device.get(deviceID).dequeueIORB();
    	 
        //10.
        if (device != null) 
    	{
    		Device.get(deviceID).startIO(device);
    	}

        //11. 
        ThreadCB.dispatch();
    }


}

