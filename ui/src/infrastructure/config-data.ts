export class ConfigData {

  private static readonly REQUIRED_CONFIG_KEYS = ['intervals.api-key', 'intervals.athlete-id'];

  config: Record<
    string,
    string | boolean | null
  > = {};

  constructor(
    config: Record<
      string,
      string | null | undefined
    >
  ) {
    Object.keys(config).forEach(key => {
      const value = config[key];

      if (
        value === '' || value === null || value === undefined
      ) {
        this.config[key] = null;
      } else if (value === 'true') {
        this.config[key] = true;
      } else if (value === 'false') {
        this.config[key] = false;
      } else {
        this.config[key] = value;
      }
    });
  }

  hasRequiredConfig(): boolean {
    return ConfigData.REQUIRED_CONFIG_KEYS.every(
      key => !!this.config[key]
    );
  }
}