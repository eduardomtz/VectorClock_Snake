/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vectorclock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author EMARTINENE
 */
public class VectorClock extends HashMap<String, Integer> implements Serializable
{
	// Unique Serial.
	private static final long serialVersionUID = 6668164199894268488L;

	/**
	 * Increases the component of pUnit by 1.
	 * 
	 * @param pUnit - The ID of the vector element being increased.
	 */
	public void incrementClock(String pUnit)
	{
		// If we have it in the vector, increment.
		if (this.containsKey(pUnit))
		{
			this.put(pUnit, this.get(pUnit).intValue() + 1);
		}
		// Else, store with value 1 (starts at 0, +1).
		else
		{
			this.put(pUnit, 1);
		}
	}
        
        /**
         * Establecer un vector desde valores conocidos
         */
        public void set(String pUnit, Integer valor){
            this.put(pUnit, valor);
        }
        
	/**
	 * GUI operation, returns the IDs in some neat order.
	 * 
	 * @return The IDs of the elements in the Clock.
	 */
	public String[] getOrderedIDs()
	{
		String[] lResult = new String[this.size()];

		lResult = this.keySet().toArray(lResult);

		Arrays.sort(lResult);

		return lResult;
	}

	/**
	 * GUI operation, returns the values in some neat order.
	 * 
	 * @return The Values of the elements in the Clock.
	 */
	public Integer[] getOrderedValues()
	{
		Integer[] lResult = new Integer[this.size()];
		String[] lKeySet  = this.getOrderedIDs();

		int i = 0;
		for (String lKey : lKeySet)
		{
			lResult[i] = this.get(lKey);
			i++;
		}

		return lResult;
	}
        
        /**
	 * GUI operation, returns the values in some neat order, excepto un elemento
	 * 
	 * @return The Values of the elements in the Clock.
	 */
	public Integer[] getOrderedValues(String pID)
	{
            if(this.size() > 0)
            {
		Integer[] lResult = new Integer[this.size()];
		String[] lKeySet  = this.getOrderedIDs();

		int i = 0;
		for (String lKey : lKeySet)
		{
                    if(this.containsKey(lKey))
                    {
                        if(!pID.equals(lKey))
                        {
                            lResult[i] = this.get(lKey);
                            i++;
                        }
                    }
		}

		return lResult;
            }
            else
            {
                Integer[] lResult = new Integer[0];
                return lResult;
            }
	}

	@Override
	public Integer get(Object key)
	{
		Integer lResult = super.get(key);

		if (lResult == null)
			lResult = 0;

		return lResult;
	}

	@Override
	public VectorClock clone()
	{
		return (VectorClock) super.clone();
	}

	@Override
	public String toString()
	{
		String[] lIDs = this.getOrderedIDs();
		Integer[] lRequests = this.getOrderedValues();

		String lText = "{";

		for (int i = 0; i < lRequests.length; i++)
		{
			lText += lIDs[i];
			lText += " = ";
			lText += lRequests[i].toString();

			if (i + 1 < lRequests.length)
			{
				lText += ", ";
			}
		}

		lText += "}";

		return lText;
	}
}