import React from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
// tslint:disable-next-line:no-unused-variable
import { ICrudGetAction, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './car-service.reducer';
import { ICarService } from 'app/shared/model/car-service.model';
// tslint:disable-next-line:no-unused-variable
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface ICarServiceDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export class CarServiceDetail extends React.Component<ICarServiceDetailProps> {
  componentDidMount() {
    this.props.getEntity(this.props.match.params.id);
  }

  render() {
    const { carServiceEntity } = this.props;
    return (
      <Row>
        <Col md="8">
          <h2>
            CarService [<b>{carServiceEntity.id}</b>]
          </h2>
          <dl className="jh-entity-details">
            <dt>
              <span id="date">Date</span>
            </dt>
            <dd>
              <TextFormat value={carServiceEntity.date} type="date" format={APP_LOCAL_DATE_FORMAT} />
            </dd>
            <dt>
              <span id="description">Description</span>
            </dt>
            <dd>{carServiceEntity.description}</dd>
            <dt>
              <span id="totalCost">Total Cost</span>
            </dt>
            <dd>{carServiceEntity.totalCost}</dd>
            <dt>Car</dt>
            <dd>{carServiceEntity.carId ? carServiceEntity.carId : ''}</dd>
          </dl>
          <Button tag={Link} to="/entity/car-service" replace color="info">
            <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
          </Button>
          &nbsp;
          <Button tag={Link} to={`/entity/car-service/${carServiceEntity.id}/edit`} replace color="primary">
            <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
          </Button>
        </Col>
      </Row>
    );
  }
}

const mapStateToProps = ({ carService }: IRootState) => ({
  carServiceEntity: carService.entity
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(CarServiceDetail);
