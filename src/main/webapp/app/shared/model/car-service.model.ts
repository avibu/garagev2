import { Moment } from 'moment';

export interface ICarService {
  id?: number;
  date?: Moment;
  description?: string;
  totalCost?: number;
  carId?: number;
}

export const defaultValue: Readonly<ICarService> = {};
