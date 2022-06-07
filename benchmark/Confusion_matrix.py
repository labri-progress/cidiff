#!/usr/bin/env python3

import numpy as np
import pandas as pd 
import matplotlib.pyplot as plt
import seaborn as sns


path_to_csv = "/home/.../precision_recall.csv"
columns = ['errors(groundtruth)','errors(Cidiff)','not errors(Cidiff)','total(Cidiff)']

df = pd.read_csv(path_to_csv, usecols=columns, sep = ';')

data = {'errors (Cidiff)': [0, 0],
        'not errors (Cidiff)': [0, 0]}
 
cf_matrix = pd.DataFrame(data, index=['errors (groundtruth)',
                               'not errors (groundtruth)'])

for (_,errors_G,errors_C,not_errors_C,total_C) in df.itertuples(name=None):
    cf_matrix.at['errors (groundtruth)', 'errors (Cidiff)'] += min(errors_G,errors_C)
    cf_matrix.at['not errors (groundtruth)', 'not errors (Cidiff)'] += min(total_C-errors_G,not_errors_C)
    cf_matrix.at['errors (groundtruth)', 'not errors (Cidiff)'] += max(errors_G-errors_C,0)
    cf_matrix.at['not errors (groundtruth)', 'errors (Cidiff)'] += max(errors_C-errors_G,0)

group_counts = cf_matrix.values.reshape(-1)
percent = cf_matrix.div(cf_matrix.sum(axis=1), axis=0).values.reshape(-1)
group_percentages =['\n'+str(round(x*100,2))+'%' for x in percent] 

labels = [f"{v2}\n{v3}" for v2, v3 in zip(group_counts,group_percentages)]
labels = np.asarray(labels).reshape(2,2)

res = sns.heatmap(cf_matrix, annot=labels, fmt="")
precision = cf_matrix.at['errors (groundtruth)', 'errors (Cidiff)']/(cf_matrix.at['errors (groundtruth)', 'errors (Cidiff)']+cf_matrix.at['not errors (groundtruth)', 'errors (Cidiff)'])
rappel = cf_matrix.at['errors (groundtruth)', 'errors (Cidiff)']/(cf_matrix.at['errors (groundtruth)', 'errors (Cidiff)']+cf_matrix.at['errors (groundtruth)', 'not errors (Cidiff)'])
plt.title(f'Confusion matrix (row normalisation) \n precision = {round(precision,3)}, rappel = {round(rappel,3)}') 
plt.show()