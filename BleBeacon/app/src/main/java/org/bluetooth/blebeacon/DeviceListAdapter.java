package org.bluetooth.blebeacon;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceListAdapter extends BaseAdapter 
{
	
	private static final String LOG = null;
	private ArrayList<BluetoothDevice> mDevices;
	private ArrayList<byte[]> mRecords;
	private ArrayList<Integer> mRSSIs;
	private ArrayList<String> mDeviceIDs;
	private LayoutInflater mInflater;
	private boolean mIntelBeacon;
    private BluetoothAdapter mBluetoothAdapter;
	
	public DeviceListAdapter(Activity par) 
	{
		super();
		mDevices  = new ArrayList<BluetoothDevice>();
		mRecords = new ArrayList<byte[]>();
		mRSSIs = new ArrayList<Integer>();
		mDeviceIDs = new ArrayList<String>();
		mInflater = par.getLayoutInflater();
	}
	
	public void addDevice(BluetoothDevice device, int rssi, byte[] scanRecord, String devId) 
	{
		if(mDevices.contains(device) == false) 
		{
			mDevices.add(device);
			mRSSIs.add(rssi);
			mRecords.add(scanRecord);
			mDeviceIDs.add(devId);
		}
	}
	
	public BluetoothDevice getDevice(int index) 
	{
		return mDevices.get(index);
	}
	
	public int getRssi(int index) 
	{
		return mRSSIs.get(index);
	}
	
	public void clearList() 
	{
		mDevices.clear();
		mRSSIs.clear();
		mRecords.clear();
		mDeviceIDs.clear();
	}
	
	@Override
	public int getCount() 
	{
		return mDevices.size();
	}

	@Override
	public Object getItem(int position) 
	{
		return getDevice(position);
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		
		// get already available view or create new if necessary
		FieldReferences fields;
        if (convertView == null) 
        {
        	convertView = mInflater.inflate(R.layout.activity_scanning_item, null);
        	fields = new FieldReferences();
        	fields.deviceAddress = (TextView)convertView.findViewById(R.id.deviceAddress);
        	fields.deviceName    = (TextView)convertView.findViewById(R.id.deviceName);
        	fields.deviceRssi    = (TextView)convertView.findViewById(R.id.deviceRssi);
			fields.deviceID      = (TextView)convertView.findViewById(R.id.deviceID);
            convertView.setTag(fields);
        } 
        else 
        {
            fields = (FieldReferences) convertView.getTag();
        }			
		
        // set proper values into the view
        BluetoothDevice device = mDevices.get(position);
        int rssi = mRSSIs.get(position);
        String rssiString = (rssi == 0) ? "N/A" : rssi + " db";
        String name = device.getName();
        String address = device.getAddress();
        if(name == null || name.length() <= 0) name = "Unknown Device";
		
        ///String uuid = mDeviceIDs.get(position);
	
		byte[] scanrecs = mRecords.get(position);
		
		StringBuilder mSbUUID = new StringBuilder();
		for (int i = 0; i < scanrecs.length; i++)
		{
	        // UUID
	        if (i >= 9 & i <= 24) 
	        {
	            if (Integer.toHexString(scanrecs[i]).contains("ffffff")) 
	            {
	                mSbUUID.append(Integer.toHexString(scanrecs[i]).replace("ffffff", "") + "-");
	            } 
	            else 
	            {
	            	if (i < 24)
	            		mSbUUID.append(Integer.toHexString(scanrecs[i]) + "-");
	            	else
	            		mSbUUID.append(Integer.toHexString(scanrecs[i]));
	            }
	        }			
		}
		
		boolean miBeacon;
		if ((scanrecs[5] == 0x4C) && (scanrecs[6] == 0x00) && (scanrecs[7] == 0x02) && (scanrecs[8] == 0x15)) // Apple Pre-amble
		{
			miBeacon = true;
		}
		else
		{
			miBeacon = false;
		}
		
		///if ((miBeacon) && ((scanrecs[9] == 0x77) && (scanrecs[10] == 0x77) && (scanrecs[11] == 0x77) && (scanrecs[12] == 0x2E)
		///&& (scanrecs[13] == 0x69) && (scanrecs[14] == 0x6E) && (scanrecs[15] == 0x74) && (scanrecs[16] == 0x65) && (scanrecs[17] == 0x6C)))
		if ((miBeacon) && ((scanrecs[9] == 0xB9) && (scanrecs[10] == 0x40) && (scanrecs[11] == 0x7F) && (scanrecs[12] == 0x30)
		&& (scanrecs[13] == 0xF5) && (scanrecs[14] == 0xF8) && (scanrecs[15] == 0x46) && (scanrecs[16] == 0x6E) && (scanrecs[17] == 0xAF)))	
		{
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			name = "IntelBeacon";
			mIntelBeacon = true;
			
		    ///void ChangeDeviceName(){
		    Log.i(LOG, "localdevicename : "+mBluetoothAdapter.getName()+" localdeviceAddress : "+mBluetoothAdapter.getAddress());
		    mBluetoothAdapter.setName(name);
		    Log.i(LOG, "localdevicename : "+mBluetoothAdapter.getName()+" localdeviceAddress : "+mBluetoothAdapter.getAddress());
		    ///}
		    
	        ///Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.intel.com"));
	        ///Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dining.guckenheimer.com/intelsc"));
	        ///startActivity(browserIntent);
		}
		else
		{
			mIntelBeacon = false;
		}		
		
        fields.deviceName.setText(name);
        fields.deviceAddress.setText(address);
        fields.deviceRssi.setText(rssiString);
        fields.deviceID.setText(mSbUUID);
        
        if ((miBeacon) && (mIntelBeacon))
        	fields.deviceName.setTextColor(Color.rgb(200,0,0));
        else if (miBeacon)
        	fields.deviceName.setTextColor(Color.rgb(0,200,0));

		return convertView;
	}
	
	private class FieldReferences 
	{
		TextView deviceName;
		TextView deviceAddress;
		TextView deviceRssi;
		TextView deviceID;
	}
}
