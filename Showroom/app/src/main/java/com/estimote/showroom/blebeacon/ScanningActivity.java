package com.estimote.showroom.blebeacon;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

//import org.bluetooth.blebeacon.R;

///import android.os.ParcelUuid;

///import java.lang.reflect.Method;
///import java.util.UUID;

public class ScanningActivity extends ListActivity
{
	
	private static final long SCANNING_TIMEOUT = 5 * 1000; /* 5 seconds */
	private static final int ENABLE_BT_REQUEST_ID = 1;
	
	private boolean mScanning = false;
	private Handler mHandler = new Handler();
	private DeviceListAdapter mDevicesListAdapter = null;
	private BleWrapper mBleWrapper = null;
	private boolean mBeacon = false;
	
	///private ArrayList<byte[]> mRecs;
	///public byte mPosition = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // create BleWrapper with empty callback object except uiDeficeFound function (we need only that here) 
        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() 
        {
        	@Override
        	public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record, final String deviceid)
        	{
				handleFoundDevice(device, rssi, record, deviceid);
        	}
        });
        
        // check if we have BT and BLE on board
        if(mBleWrapper.checkBleHardwareAvailable() == false) 
        {
        	bleMissing();
        }
    }

    @Override
    protected void onResume() 
    {
    	super.onResume();
    	
    	// on every Resume check if BT is enabled (user could turn it off while app was in background etc.)
    	if(mBleWrapper.isBtEnabled() == false) 
    	{
			// BT is not turned on - ask user to make it enabled
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
		    // see onActivityResult to check what is the status of our request
		}
    	
    	// initialize BleWrapper object
        mBleWrapper.initialize();
    	
    	mDevicesListAdapter = new DeviceListAdapter(this);
        setListAdapter(mDevicesListAdapter);
        // Automatically start scanning for devices
    	mScanning = true;
		// remember to add timeout for scanning to not run it forever and drain the battery
		addScanningTimeout();    	
		mBleWrapper.startScanning();
        invalidateOptionsMenu();
    };
    
    @Override
    protected void onPause() 
    {
    	super.onPause();
    	mScanning = false;    	
    	mBleWrapper.stopScanning();
    	invalidateOptionsMenu();
    	mDevicesListAdapter.clearList();
    };
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.scanning, menu);
//
//        if (mScanning)
//        {
//            menu.findItem(R.id.scanning_start).setVisible(false);
//            menu.findItem(R.id.scanning_stop).setVisible(true);
//            menu.findItem(R.id.scanning_indicator)
//                .setActionView(R.layout.progress_indicator);
//
//        }
//        else
//        {
//            menu.findItem(R.id.scanning_start).setVisible(true);
//            menu.findItem(R.id.scanning_stop).setVisible(false);
//            menu.findItem(R.id.scanning_indicator).setActionView(null);
//        }
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        switch (item.getItemId())
//        {
//            case R.id.scanning_start:
//            	mScanning = true;
//            	mBleWrapper.startScanning();
//                break;
//            case R.id.scanning_stop:
//            	mScanning = false;
//            	mBleWrapper.stopScanning();
//                break;
//            case R.id.show_hr_demo_item:
//            	startHRDemo();
//            	break;
//        }
//
//        invalidateOptionsMenu();
//        return true;
//    }

    private void startHRDemo() 
    {
        startActivity(new Intent(this, HRDemoActivity.class));
    }
    
    /* user has selected one of the device */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        final BluetoothDevice device = mDevicesListAdapter.getDevice(position);
        if (device == null) return;

        String Nname = "IntelBeacon";
        
        String beaname = device.getName();
        if (Nname.equals(device.getName()));

        {
        	
            final Intent intent = new Intent(this, PeripheralActivity.class);
            intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_NAME, device.getName());
            intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
            intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_RSSI, mDevicesListAdapter.getRssi(position));        	
        	
        	
        	///Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.intel.com"));
        	
        	///Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.panerabread.com"));
        	
        	startActivity(intent);
        }
        
		///Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.intel.com"));
        ///else
        /*{
        	final Intent intent = new Intent(this, PeripheralActivity.class);
        	intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_NAME, device.getName());
        	intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        	intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_RSSI, mDevicesListAdapter.getRssi(position));
        	///intent.putExtra(PeripheralActivity.EXTRAS_DEVICE_UUID, device.getUuids());
        
        	if (mScanning) 
        	{
        		mScanning = false;
        		invalidateOptionsMenu();
        		mBleWrapper.stopScanning();
        	}

        	startActivity(intent);
        }*/
    }    
    
    /* check if user agreed to enable BT */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // user didn't want to turn on BT
        if (requestCode == ENABLE_BT_REQUEST_ID) 
        {
        	if(resultCode == Activity.RESULT_CANCELED)
        	{
		    	btDisabled();
		        return;
		    }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

	/* make sure that potential scanning will take no longer
	 * than <SCANNING_TIMEOUT> seconds from now on */
	private void addScanningTimeout() 
	{
		Runnable timeout = new Runnable()
		{
            @Override
            public void run() 
            {
            	if(mBleWrapper == null) return;
                mScanning = false;
                mBleWrapper.stopScanning();
                invalidateOptionsMenu();
            }
        };
        mHandler.postDelayed(timeout, SCANNING_TIMEOUT);
	}    

	/* add device to the current list of devices */
    private void handleFoundDevice(final BluetoothDevice device, final int rssi, final byte[] scanRecord, final String devid)
	{
		// adding to the UI have to happen in UI thread
		runOnUiThread(new Runnable()
		{
			@Override
			public void run() 
			{
				/*BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
				ParcelUuid[] mDeviceUUID = device.getUuids();
				
				///final String struuid = mDeviceUUID.toString();
				///mDeviceUUID.toString();
				try 
				{
					Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
				} 
				catch (NoSuchMethodException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ParcelUuid[] mDeviceUUID = device.getUuids(); 
				
				///for (ParcelUuid uuid: mDeviceUUID) 
				///    Log.d(TAG, "UUID: " + uuid.getUuid().toString());

				mDeviceUUID.toString();*/
				
				if ((scanRecord[5] == 0x4C) && (scanRecord[6] == 0x00) && (scanRecord[7] == 0x02) && (scanRecord[8] == 0x15)) // Apple Pre-amble
					mBeacon = true;
				else
					mBeacon = false;
				
				if ((scanRecord[9] == 0x77) && (scanRecord[10] == 0x77) && (scanRecord[11] == 0x77))
				{
					BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
					BTAdapter.setName("IntelBeacon");
				}
				
				///if ((scanRecord[9] == 0xB9) && (scanRecord[10] == 0x40) && (scanRecord[11] == 0x7F) && (scanRecord[12] == 0x30))
				///if ((scanRecord[9] == 0x77) && (scanRecord[10] == 0x77) && (scanRecord[11] == 0x77))
				{
					///BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
					///BTAdapter.setName("IntelBeacon");
					
					///name = "IntelBeacon";
					///mIntelBeacon = true;
			        ///Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dining.guckenheimer.com/intelsc"));
			        ///startActivity(browserIntent);
					
					///mDevicesListAdapter.addDevice(device, rssi, scanRecord, devid);
					///mDevicesListAdapter.notifyDataSetChanged();					
				}
					
				if (mBeacon)
				{
					mDevicesListAdapter.addDevice(device, rssi, scanRecord, devid);
					mDevicesListAdapter.notifyDataSetChanged();
				}
			}
		});
	}	

    private void btDisabled() 
    {
    	Toast.makeText(this, "Sorry, BT has to be turned ON for us to work!", Toast.LENGTH_LONG).show();
        finish();    	
    }
    
    private void bleMissing() 
    {
    	Toast.makeText(this, "BLE Hardware is required but not available!", Toast.LENGTH_LONG).show();
        finish();    	
    }
}
