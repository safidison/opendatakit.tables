package org.opendatakit.tables.fragments;

import static org.fest.assertions.api.ANDROID.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.tables.activities.TableDisplayActivityStub;
import org.opendatakit.tables.utils.Constants;
import org.opendatakit.testutils.ODKFragmentTestUtil;
import org.opendatakit.testutils.TestCaseUtils;
import org.opendatakit.testutils.TestConstants;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

@RunWith(RobolectricTestRunner.class)
public class AbsWebTableFragmentTest {
  
  AbsWebTableFragmentStub fragment;
  Activity activity;
  
  @Before
  public void before() {
    TestCaseUtils.setThreeTableDataset();
    ShadowLog.stream = System.out;
    TestCaseUtils.setExternalStorageMounted();
    TableDisplayActivityStub.BUILD_MENU_FRAGMENT = false;
  }
  
  @After
  public void after() {
    TestCaseUtils.resetExternalStorageState();
    TableDisplayActivityStub.resetState();
  }
  
  private void setupFragmentWithDefaultFileName() {
    AbsWebTableFragmentStub stub = new AbsWebTableFragmentStub();
    this.doGlobalSetup(stub);
  }
  
  private void setupFragmentWithFileName(String fileName) {
    AbsWebTableFragmentStub stub = new AbsWebTableFragmentStub();
    Bundle bundle = new Bundle();
    bundle.putString(Constants.IntentKeys.FILE_NAME, fileName);
    stub.setArguments(bundle);
    this.doGlobalSetup(stub);
  }
  
  private void doGlobalSetup(AbsWebTableFragmentStub stub) {
    this.fragment = stub;
    ODKFragmentTestUtil.startFragmentForActivity(
        TableDisplayActivityStub.class,
        stub,
        null);
    this.activity = this.fragment.getActivity();
  }
  
  @Test
  public void fragmentInitializesNonNull() {
    this.setupFragmentWithFileName("testFileName");
    assertThat(this.fragment).isNotNull();
  }
  
  @Test
  public void fragmentStoresCorrectFileName() {
    String target = "test/path/to/file";
    this.setupFragmentWithFileName(target);
    org.fest.assertions.api.Assertions.assertThat(this.fragment.getFileName())
        .isEqualTo(target);
  }
  
  @Test
  public void setsTheViewReturnedByBuildCustomView() {
    WebView mockWebView = TestConstants.getWebViewMock();
    AbsWebTableFragmentStub.WEB_VIEW = mockWebView;
    this.setupFragmentWithFileName("testFileName");
    View fragmentView = this.fragment.getView();
    assertThat(fragmentView).isSameAs(mockWebView);
  }

}
