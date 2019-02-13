import axios from 'axios';
import { ICrudSearchAction, ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { ICarService, defaultValue } from 'app/shared/model/car-service.model';

export const ACTION_TYPES = {
  SEARCH_CARSERVICES: 'carService/SEARCH_CARSERVICES',
  FETCH_CARSERVICE_LIST: 'carService/FETCH_CARSERVICE_LIST',
  FETCH_CARSERVICE: 'carService/FETCH_CARSERVICE',
  CREATE_CARSERVICE: 'carService/CREATE_CARSERVICE',
  UPDATE_CARSERVICE: 'carService/UPDATE_CARSERVICE',
  DELETE_CARSERVICE: 'carService/DELETE_CARSERVICE',
  RESET: 'carService/RESET'
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<ICarService>,
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false
};

export type CarServiceState = Readonly<typeof initialState>;

// Reducer

export default (state: CarServiceState = initialState, action): CarServiceState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.SEARCH_CARSERVICES):
    case REQUEST(ACTION_TYPES.FETCH_CARSERVICE_LIST):
    case REQUEST(ACTION_TYPES.FETCH_CARSERVICE):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true
      };
    case REQUEST(ACTION_TYPES.CREATE_CARSERVICE):
    case REQUEST(ACTION_TYPES.UPDATE_CARSERVICE):
    case REQUEST(ACTION_TYPES.DELETE_CARSERVICE):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true
      };
    case FAILURE(ACTION_TYPES.SEARCH_CARSERVICES):
    case FAILURE(ACTION_TYPES.FETCH_CARSERVICE_LIST):
    case FAILURE(ACTION_TYPES.FETCH_CARSERVICE):
    case FAILURE(ACTION_TYPES.CREATE_CARSERVICE):
    case FAILURE(ACTION_TYPES.UPDATE_CARSERVICE):
    case FAILURE(ACTION_TYPES.DELETE_CARSERVICE):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload
      };
    case SUCCESS(ACTION_TYPES.SEARCH_CARSERVICES):
    case SUCCESS(ACTION_TYPES.FETCH_CARSERVICE_LIST):
      return {
        ...state,
        loading: false,
        totalItems: action.payload.headers['x-total-count'],
        entities: action.payload.data
      };
    case SUCCESS(ACTION_TYPES.FETCH_CARSERVICE):
      return {
        ...state,
        loading: false,
        entity: action.payload.data
      };
    case SUCCESS(ACTION_TYPES.CREATE_CARSERVICE):
    case SUCCESS(ACTION_TYPES.UPDATE_CARSERVICE):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data
      };
    case SUCCESS(ACTION_TYPES.DELETE_CARSERVICE):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: {}
      };
    case ACTION_TYPES.RESET:
      return {
        ...initialState
      };
    default:
      return state;
  }
};

const apiUrl = 'api/car-services';
const apiSearchUrl = 'api/_search/car-services';

// Actions

export const getSearchEntities: ICrudSearchAction<ICarService> = (query, page, size, sort) => ({
  type: ACTION_TYPES.SEARCH_CARSERVICES,
  payload: axios.get<ICarService>(`${apiSearchUrl}?query=${query}${sort ? `&page=${page}&size=${size}&sort=${sort}` : ''}`)
});

export const getEntities: ICrudGetAllAction<ICarService> = (page, size, sort) => {
  const requestUrl = `${apiUrl}${sort ? `?page=${page}&size=${size}&sort=${sort}` : ''}`;
  return {
    type: ACTION_TYPES.FETCH_CARSERVICE_LIST,
    payload: axios.get<ICarService>(`${requestUrl}${sort ? '&' : '?'}cacheBuster=${new Date().getTime()}`)
  };
};

export const getEntity: ICrudGetAction<ICarService> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_CARSERVICE,
    payload: axios.get<ICarService>(requestUrl)
  };
};

export const createEntity: ICrudPutAction<ICarService> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_CARSERVICE,
    payload: axios.post(apiUrl, cleanEntity(entity))
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<ICarService> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_CARSERVICE,
    payload: axios.put(apiUrl, cleanEntity(entity))
  });
  dispatch(getEntities());
  return result;
};

export const deleteEntity: ICrudDeleteAction<ICarService> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_CARSERVICE,
    payload: axios.delete(requestUrl)
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET
});
