import React from 'react';
import { DropdownItem } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { NavLink as Link } from 'react-router-dom';
import { NavDropdown } from '../header-components';

export const EntitiesMenu = props => (
  // tslint:disable-next-line:jsx-self-close
  <NavDropdown icon="th-list" name="Entities" id="entity-menu">
    <DropdownItem tag={Link} to="/entity/client">
      <FontAwesomeIcon icon="asterisk" fixedWidth />
      &nbsp;Client
    </DropdownItem>
    <DropdownItem tag={Link} to="/entity/car">
      <FontAwesomeIcon icon="asterisk" fixedWidth />
      &nbsp;Car
    </DropdownItem>
    <DropdownItem tag={Link} to="/entity/car-service">
      <FontAwesomeIcon icon="asterisk" fixedWidth />
      &nbsp;Car Service
    </DropdownItem>
    {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
  </NavDropdown>
);
