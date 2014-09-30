package org.opendatakit.tables.fragments;

import java.util.ArrayList;

import org.opendatakit.common.android.data.ColorRuleGroup;
import org.opendatakit.common.android.data.ColumnDefinition;
import org.opendatakit.common.android.database.DataModelDatabaseHelperFactory;
import org.opendatakit.tables.R;
import org.opendatakit.tables.activities.TableLevelPreferencesActivity;
import org.opendatakit.tables.utils.ColumnUtil;
import org.opendatakit.tables.utils.Constants;
import org.opendatakit.tables.utils.ElementTypeManipulator;
import org.opendatakit.tables.utils.ElementTypeManipulator.ITypeManipulatorFragment;
import org.opendatakit.tables.utils.ElementTypeManipulatorFactory;
import org.opendatakit.tables.utils.PreferenceUtil;
import org.opendatakit.tables.views.SpreadsheetView;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.Log;

public class ColumnPreferenceFragment extends AbsTableLevelPreferenceFragment {

  private static final String TAG =
      ColumnPreferenceFragment.class.getSimpleName();

  public ColumnPreferenceFragment() {
    // required for fragments.
  }

  public void onAttach(android.app.Activity activity) {
    super.onAttach(activity);
    if (!(activity instanceof TableLevelPreferencesActivity)) {
      throw new IllegalStateException("fragment must be attached to "
          + TableLevelPreferencesActivity.class.getSimpleName());
    }
  }

  /**
   * Get the {@link TableLevelPreferencesActivity} associated with this
   * activity.
   */
  TableLevelPreferencesActivity retrieveTableLevelPreferenceActivity() {
    TableLevelPreferencesActivity result =
        (TableLevelPreferencesActivity) this.getActivity();
    return result;
  }

  /**
   * Retrieve the {@link ColumnDefinition} associated with the column this
   * activity is displaying.
   * 
   * @return
   */
  ColumnDefinition retrieveColumnDefinition() {
    TableLevelPreferencesActivity activity = retrieveTableLevelPreferenceActivity();
    String elementKey = activity.getElementKey();
    try {
      ArrayList<ColumnDefinition> orderedDefns = activity.getColumnDefinitions();
      ColumnDefinition result =
          ColumnDefinition.find(orderedDefns, elementKey);
      return result;
    } catch ( IllegalArgumentException e ) {
      Log.e(
          TAG,
          "[retrieveColumnDefinition] did not find column for element key: " +
              elementKey);
      return null;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.addPreferencesFromResource(R.xml.preference_column);
  }

  @Override
  public void onResume() {
    super.onResume();
    this.initializeAllPreferences();
  }

  void initializeAllPreferences() {
    this.initializeColumnType();
    this.initializeColumnWidth();
    this.initializeDisplayName();
    this.initializeElementKey();
    this.initializeElementName();
    this.initializeColorRule();
  }

  private void initializeDisplayName() {
    EditTextPreference pref = this
        .findEditTextPreference(Constants.PreferenceKeys.Column.DISPLAY_NAME);

    String rawDisplayName;
    SQLiteDatabase db = null;
    try {
      db = DataModelDatabaseHelperFactory.getDatabase(getActivity(), getAppName());
      rawDisplayName = ColumnUtil.get().getRawDisplayName(db, getTableId(), 
          this.retrieveColumnDefinition().getElementKey());
    } finally {
      if ( db != null ) {
        db.close();
      }
    }

    pref.setSummary(rawDisplayName);

  }

  private void initializeElementKey() {
    EditTextPreference pref = this
        .findEditTextPreference(Constants.PreferenceKeys.Column.ELEMENT_KEY);
    pref.setSummary(this.retrieveColumnDefinition().getElementKey());
  }

  private void initializeColumnType() {
    EditTextPreference pref =
        this.findEditTextPreference(Constants.PreferenceKeys.Column.TYPE);
    ElementTypeManipulator m = ElementTypeManipulatorFactory.getInstance();
    ITypeManipulatorFragment r = m.getDefaultRenderer(this.retrieveColumnDefinition().getType());
    pref.setSummary(r.getElementTypeDisplayLabel());
  }

  private void initializeElementName() {
    EditTextPreference pref = this
        .findEditTextPreference(Constants.PreferenceKeys.Column.DISPLAY_NAME);
    pref.setSummary(this.retrieveColumnDefinition().getElementName());
  }

  private void initializeColumnWidth() {
    final EditTextPreference pref =
        this.findEditTextPreference(Constants.PreferenceKeys.Column.WIDTH);
    int columnWidth = PreferenceUtil.getColumnWidth(getActivity(),
        getAppName(), getTableId(),
        retrieveColumnDefinition().getElementKey());
    pref.setSummary(Integer.toString(columnWidth));

    pref.setOnPreferenceChangeListener(
        new Preference.OnPreferenceChangeListener() {

      @Override
      public boolean onPreferenceChange(
          Preference preference,
          Object newValue) {
        String newValueStr = (String) newValue;
        Integer newWidth = Integer.parseInt(newValueStr);
        if (newWidth > SpreadsheetView.MAX_COL_WIDTH) {
          Log.e(TAG, "column width bigger than allowed, doing nothing");
          return false;
        }
        PreferenceUtil.setColumnWidth(
            getActivity(), getAppName(), getTableId(),
            retrieveColumnDefinition().getElementKey(),
            newWidth);
        pref.setSummary(Integer.toString(newWidth));
        return true;
      }
    });

  }
  
  private void initializeColorRule() {
    Preference pref =
        this.findPreference(Constants.PreferenceKeys.Column.COLOR_RULES);
    pref.setOnPreferenceClickListener(
        new Preference.OnPreferenceClickListener() {
      
      @Override
      public boolean onPreferenceClick(Preference preference) {
        TableLevelPreferencesActivity activity =
            (TableLevelPreferencesActivity) getActivity();
        activity.showColorRuleListFragment(
            retrieveColumnDefinition().getElementKey(),
            ColorRuleGroup.Type.COLUMN);
        return true;
      }
      
    });
  }

}
