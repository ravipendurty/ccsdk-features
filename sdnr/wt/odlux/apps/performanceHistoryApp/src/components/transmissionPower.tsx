/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
import * as React from 'react';

import { withRouter, RouteComponentProps } from 'react-router-dom';

import { MaterialTable, ColumnType, ColumnModel, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { TransmissionPowerDataType, TransmissionPowerDatabaseDataType } from '../models/transmissionPowerDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createTransmissionPowerProperties, createTransmissionPowerActions } from '../handlers/transmissionPowerHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';
import { SetSubViewAction, SetFilterVisibility } from '../actions/toggleActions';
import ToggleContainer from './toggleContainer';

const mapProps = (state: IApplicationStoreState) => ({
  transmissionPowerProperties: createTransmissionPowerProperties(state),
  currentView: state.performanceHistory.subViews.transmissionPower.subView,
  isFilterVisible: state.performanceHistory.subViews.transmissionPower.isFilterVisible,
  existingFilter: state.performanceHistory.transmissionPower.filter
});

const mapDisp = (dispatcher: IDispatcher) => ({
  transmissionPowerActions: createTransmissionPowerActions(dispatcher.dispatch),
  setSubView: (value: "chart" | "table") => dispatcher.dispatch(new SetSubViewAction("transmissionPower", value)),
  toggleFilterButton: (value: boolean) => { dispatcher.dispatch(new SetFilterVisibility("transmissionPower", value)) },


});

type TransmissionPowerComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
}

const TransmissionPowerTable = MaterialTable as MaterialTableCtorType<TransmissionPowerDataType>;

/**
 * The Component which gets the transmission power data from the database based on the selected time period.
 */
class TransmissionPowerComponent extends React.Component<TransmissionPowerComponentProps>{

  onToggleFilterButton = () => {
    this.props.toggleFilterButton(!this.props.isFilterVisible);
  }

  onChange = (value: "chart" | "table") => {
    this.props.setSubView(value);
  }

  render(): JSX.Element {
    const properties = this.props.transmissionPowerProperties
    const actions = this.props.transmissionPowerActions

    const chartPagedData = this.getChartDataValues(properties.rows);

    const transmissionColumns: ColumnModel<TransmissionPowerDataType>[] = [
      { property: "radioSignalId", title: "Radio signal", type: ColumnType.text },
      { property: "scannerId", title: "Scanner ID", type: ColumnType.text },
      { property: "timeStamp", title: "End Time", type: ColumnType.text },
      {
        property: "suspectIntervalFlag", title: "Suspect Interval", type: ColumnType.boolean
      }
    ];

    chartPagedData.datasets.forEach(ds => {
      transmissionColumns.push(addColumnLabels<TransmissionPowerDataType>(ds.name, ds.columnLabel));
    });

    return (
      <>
        <ToggleContainer onChange={this.onChange} onToggleFilterButton={this.onToggleFilterButton} showFilter={this.props.isFilterVisible} existingFilter={this.props.transmissionPowerProperties.filter} onFilterChanged={this.props.transmissionPowerActions.onFilterChanged} selectedValue={this.props.currentView} >
          {lineChart(chartPagedData)}
          <TransmissionPowerTable stickyHeader idProperty={"_id"} columns={transmissionColumns} {...properties} {...actions} />
        </ToggleContainer>
      </>
    );
  };

  /**
   * This function gets the performance values for TransmissionPower according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */

  private getChartDataValues = (rows: TransmissionPowerDataType[]): IDataSetsObject => {
    const _rows = [...rows];
    sortDataByTimeStamp(_rows);

    const datasets: IDataSet[] = [{
      name: "txLevelMin",
      label: "tx-level-min",
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Tx min"
    }, {
      name: "txLevelAvg",
      label: "tx-level-avg",
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Tx avg"
    }, {
      name: "txLevelMax",
      label: "tx-level-max",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Tx max"
    }];

    _rows.forEach(row => {
      row.txLevelMin = row.performanceData.txLevelMin;
      row.txLevelAvg = row.performanceData.txLevelAvg;
      row.txLevelMax = row.performanceData.txLevelMax;
      datasets.forEach(ds => {
        ds.data.push({
          x: row["timeStamp" as keyof TransmissionPowerDataType] as string,
          y: row.performanceData[ds.name as keyof TransmissionPowerDatabaseDataType] as string
        });
      });
    });
    return {
      datasets: datasets
    };
  }
}

const TransmissionPower = withRouter(connect(mapProps, mapDisp)(TransmissionPowerComponent));
export default TransmissionPower;
