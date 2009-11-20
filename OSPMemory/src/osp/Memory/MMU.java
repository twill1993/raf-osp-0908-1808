package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;

/**
    The MMU class contains the student code that performs the work of
    handling a memory reference.  It is responsible for calling the
    interrupt handler if a page fault is required.

    @OSPProject Memory
*/
public class MMU extends IflMMU
{
	static ArrayList<FrameTableEntry> frameTable;
	static PageFaultHandler handler;
    /** 
        This method is called once before the simulation starts. 
	Can be used to initialize the frame table and other static variables.

        @OSPProject Memory
    */
    public static void init()
    {
    	frameTable = new ArrayList<FrameTableEntry>();
    	//used to initilize the frame table
        int size = MMU.getFrameTableSize();
        for(int i = 0; i < size; i++)
        {
        	FrameTableEntry fte = new FrameTableEntry(i);
        	frameTable.add(fte);
        	MMU.setFrame(i, fte);
        }
        // PageFaultHandler is able to access any variable defined in that class
        handler = new PageFaultHandler();
        
    }

    /**
       This method handlies memory references. The method must 
       calculate, which memory page contains the memoryAddress,
       determine, whether the page is valid, start page fault 
       by making an interrupt if the page is invalid, finally, 
       if the page is still valid, i.e., not swapped out by another 
       thread while this thread was suspended, set its frame
       as referenced and then set it as dirty if necessary.
       (After pagefault, the thread will be placed on the ready queue, 
       and it is possible that some other thread will take away the frame.)
       
       @param memoryAddress A virtual memory address
       @param referenceType The type of memory reference to perform 
       @param thread that does the memory access
       (e.g., MemoryRead or MemoryWrite).
       @return The referenced page.

       @OSPProject Memory
    */
    /**
     * This method takes an address of a byte in the logical memory of the thread,
     * a type of the memory reference (MemoryRead, MemoryWrite, or MemoryLock)
     * and a thread that made the reference. The method then needs to determine
     * the page of the thread’s logical memory to which the reference was made.
     * The methods getVirtualAddressBits() and getPageAddressBits(), both
     * inherited from the superclass IflMMU, can be used to determine the number
     * of bits allocated to represent the offset within the page. This number can then
     * be used to compute the page size and then the page to which memoryAddress
     * belongs.
     */
    static public PageTableEntry do_refer(int memoryAddress, int referenceType, ThreadCB thread)
    {
    	int bits = MMU.getPageAddressBits();
    	
    	return null;
    }

    /** Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures
	in their state just after the error happened.  The body can be
	left empty, if this feature is not used.
     
	@OSPProject Memory
     */
    public static void atError()
    {
        System.out.println("MMU error");
    }

    /** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.
     
      @OSPProject Memory
     */
    public static void atWarning()
    {
    	System.out.println("MMU warning");
    }

}
