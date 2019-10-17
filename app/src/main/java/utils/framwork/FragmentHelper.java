package utils.framwork;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import java.util.LinkedList;

/**
 * Created by 张建宇 on 2019/7/26.
 */
public class FragmentHelper {

    LinkedList<Fragment> mFrags;
    private Activity mActivity;

    private FragmentManager manager;
    FragmentTransaction fragmentTransaction;
    private int contViewID = -1;

    public FragmentHelper(AppCompatActivity mActivity, int contViewID) {
        this.mActivity = mActivity;
        this.contViewID = contViewID;
        manager = mActivity.getSupportFragmentManager();
    }

    public FragmentHelper(AppCompatActivity mActivity) {
        this.mActivity = mActivity;
        manager = mActivity.getSupportFragmentManager();
    }

    public void begin() {
        if (fragmentTransaction == null) {
            fragmentTransaction = manager.beginTransaction();
        }
    }

    public void commit() {
        if (fragmentTransaction == null) {
            throw new RuntimeException("请先调用begin");
        }
        int commit = fragmentTransaction.commit();
        fragmentTransaction = null;
    }

    public void replace(int cId, android.support.v4.app.Fragment frag) {
        fragmentTransaction.replace(cId, frag);
    }

    public void addFrag(int id, android.support.v4.app.Fragment mFrag) {
        fragmentTransaction.add(id, mFrag);
    }
}
