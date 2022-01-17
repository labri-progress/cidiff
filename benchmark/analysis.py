#!/usr/bin/env python3

import plotnine as pn
import pandas as pd
import statistics

def main():
    df = pd.read_csv('results.csv', sep=';')
    df['t'] = df.apply (lambda row: statistics.median([row['t1'], row['t2'], row['t3'], row['t4'], row['t5']]), axis = 1)
    p = pn.ggplot(df, pn.aes('config', 'added')) + \
        pn.geom_jitter()
    p.save('added.png', dpi=300)
    p = pn.ggplot(df, pn.aes('config', 't')) + \
        pn.geom_jitter()
    p.save('time.png', dpi=300)

if __name__ == "__main__":
    main()