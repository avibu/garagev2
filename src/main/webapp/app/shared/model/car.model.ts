import { ICarService } from 'app/shared/model/car-service.model';

export interface ICar {
  id?: number;
  licensePlate?: string;
  make?: string;
  model?: string;
  year?: number;
  clientId?: number;
  carServices?: ICarService[];
}

export const defaultValue: Readonly<ICar> = {};
