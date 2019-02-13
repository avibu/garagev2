import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import CarService from './car-service';
import CarServiceDetail from './car-service-detail';
import CarServiceUpdate from './car-service-update';
import CarServiceDeleteDialog from './car-service-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={CarServiceUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={CarServiceUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={CarServiceDetail} />
      <ErrorBoundaryRoute path={match.url} component={CarService} />
    </Switch>
    <ErrorBoundaryRoute path={`${match.url}/:id/delete`} component={CarServiceDeleteDialog} />
  </>
);

export default Routes;
