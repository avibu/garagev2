import React from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
// tslint:disable-next-line:no-unused-variable
import { ICrudGetAction } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './car.reducer';
import { ICar } from 'app/shared/model/car.model';
// tslint:disable-next-line:no-unused-variable
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface ICarDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export class CarDetail extends React.Component<ICarDetailProps> {
  componentDidMount() {
    this.props.getEntity(this.props.match.params.id);
  }

  render() {
    const { carEntity } = this.props;
    return (
      <Row>
        <Col md="8">
          <h2>
            Car [<b>{carEntity.id}</b>]
          </h2>
          <dl className="jh-entity-details">
            <dt>
              <span id="licensePlate">License Plate</span>
            </dt>
            <dd>{carEntity.licensePlate}</dd>
            <dt>
              <span id="make">Make</span>
            </dt>
            <dd>{carEntity.make}</dd>
            <dt>
              <span id="model">Model</span>
            </dt>
            <dd>{carEntity.model}</dd>
            <dt>
              <span id="year">Year</span>
            </dt>
            <dd>{carEntity.year}</dd>
            <dt>Client</dt>
            <dd>{carEntity.clientId ? carEntity.clientId : ''}</dd>
          </dl>
          <Button tag={Link} to="/entity/car" replace color="info">
            <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
          </Button>
          &nbsp;
          <Button tag={Link} to={`/entity/car/${carEntity.id}/edit`} replace color="primary">
            <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
          </Button>
        </Col>
      </Row>
    );
  }
}

const mapStateToProps = ({ car }: IRootState) => ({
  carEntity: car.entity
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(CarDetail);
