export interface IClient {
  id?: number;
  firstName?: string;
  lastName?: string;
  mail?: string;
  phoneNum?: string;
  carId?: number;
}

export const defaultValue: Readonly<IClient> = {};
